package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.types;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives.Incentive;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives.Money;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives.StationQuality;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("SURROUNDING_STATIONS_COMPLEX_INCENTIVES")
public class RecommendationSystemBySurroundingStationsWithComplexIncentives extends RecommendationSystem {
	
	private class IncentiveManagerSurroundingStations {
		private double COMPENSATION = 14;  // 1 cent per 12 meters 
		private double EXTRA = 1.01;
    
		public double qualityToRent(List<Station> stations, Station station) {
	        double summation = 0;
	        if (!stations.isEmpty()) {
	            double factor, multiplication;
	            double maxDistance = parameters.maxDistanceRecommendation;
	            double distance = 0.0;
	            stations.stream().filter(s -> s.getPosition()
	            		.distanceTo(station.getPosition()) < maxDistance)
	            .collect(Collectors.toList());

	            for (Station s : stations) {
	        						distance = station.getPosition().distanceTo(s.getPosition());
	                factor = (maxDistance - distance) / maxDistance;
	                multiplication = s.availableBikes() * factor;
	                summation += multiplication;
	            }
	        }
	        return summation;
	    }

	    public double qualityToReturn(List<Station> stations, Station station) {
	        double summation = 0;
	        if (!stations.isEmpty()) {
	            double factor, multiplication;
	            double maxDistance = parameters.maxDistanceRecommendation;
	            double distance = 0.0;
	            stations.stream().filter(s -> s.getPosition()
	            		.distanceTo(station.getPosition()) < maxDistance)
	            .collect(Collectors.toList());
	                    
	            for (Station s : stations) {
	            		distance = station.getPosition().distanceTo(s.getPosition());
	                factor = (maxDistance - distance) / maxDistance; 
	                multiplication = s.availableSlots() * factor;
	                summation += multiplication;
	            }
	        }
	        return summation;
	    }
	    
	    private double extra(StationQuality nearestStationQuality, StationQuality recommendedStationQuality) {
	        double quality1 = nearestStationQuality.getQuality();
	        double quality2 = recommendedStationQuality.getQuality();
	        return Math.abs(quality2 - quality1) * EXTRA;
	    }

	    private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
	        double distanceToNearestStation = nearestStation.getPosition().distanceTo(point);
	        double distanceToRecommendedStation = recommendedStation.getPosition().distanceTo(point);
	        return (distanceToRecommendedStation - distanceToNearestStation) / COMPENSATION;
	    }

	    public Incentive calculateIncentive(GeoPoint point, StationQuality nearestStationQuality, StationQuality recommendedStationQuality) {
	        double compensation = compensation(point, nearestStationQuality.getStation(), recommendedStationQuality.getStation());
	        double extra = extra(nearestStationQuality, recommendedStationQuality);
	        Incentive money = new Money((int)Math.round(compensation + extra));
	        return money;
	    }
	}

    @RecommendationSystemParameters
    public class RecommendationParameters {
        private int maxDistanceRecommendation = 700;
        }
    private IncentiveManagerSurroundingStations incentiveManager;

    private RecommendationParameters parameters;

    public RecommendationSystemBySurroundingStationsWithComplexIncentives(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
        super(infraestructureManager);
        incentiveManager = new IncentiveManagerSurroundingStations(); 
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just hasT to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation)
                .collect(Collectors.toList());
        List<StationQuality> qualities = new ArrayList<>();
        List<Station> allStations = validStationsToRentBike(infraestructureManager.consultStations());

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            double quality = incentiveManager.qualityToRent(allStations, station);
            qualities.add(new StationQuality(station, quality));
        }

        Station nearestStation = nearestStationToRent(stations, point);
        double nearestStationQuality = incentiveManager.qualityToRent(stations, nearestStation);
        StationQuality stationQuality = new StationQuality(nearestStation, nearestStationQuality);

        Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
        return qualities.stream().sorted(byQuality).map(sq -> {
            Station s = sq.getStation();
            Incentive incentive = new Money(0);
            if (s.getId() != nearestStation.getId()) {
                incentive = incentiveManager.calculateIncentive(point, stationQuality, sq);
            }
            return new Recommendation(s, incentive);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation)
                .collect(Collectors.toList());
        List<StationQuality> qualities = new ArrayList<>();
        List<Station> allStations = validStationsToReturnBike(infraestructureManager.consultStations());

        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            double quality = incentiveManager.qualityToReturn(allStations, station);
            qualities.add(new StationQuality(station, quality));
        }

        Station nearestStation = nearestStationToReturn(stations, point);
        double nearestStationQuality = incentiveManager.qualityToReturn(stations, nearestStation);
        StationQuality stationQuality = new StationQuality(nearestStation, nearestStationQuality);

        Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
        return qualities.stream().sorted(byQuality).map(sq -> {
            Station s = sq.getStation();
            Incentive incentive = new Money(0);
            if (s.getId() != nearestStation.getId()) {
                incentive = incentiveManager.calculateIncentive(point, stationQuality, sq);
            }
            return new Recommendation(s, incentive);
        }).collect(Collectors.toList());
    }

    
    private Station nearestStationToRent(List<Station> stations, GeoPoint point) {
        Comparator<Station> byDistance = StationComparator.byDistance(point);
        List<Station> orderedStations = stations.stream().filter(s -> s.availableBikes() > 0)
                .sorted(byDistance).collect(Collectors.toList());
        return orderedStations.get(0);
    }

    private Station nearestStationToReturn(List<Station> stations, GeoPoint point) {
        Comparator<Station> byDistance = StationComparator.byDistance(point);
        List<Station> orderedStations = stations.stream().filter(s -> s.availableSlots() > 0)
                .sorted(byDistance).collect(Collectors.toList());
        return orderedStations.get(0);
    }
}
