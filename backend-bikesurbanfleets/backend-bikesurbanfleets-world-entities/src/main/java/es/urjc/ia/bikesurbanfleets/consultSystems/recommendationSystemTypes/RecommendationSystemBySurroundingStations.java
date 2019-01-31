package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("SURROUNDING_STATIONS")
public class RecommendationSystemBySurroundingStations extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        private int maxDistanceRecommendation = 600;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemBySurroundingStations(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
        super(infraestructureManager);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
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
        Comparator<Station> byBikesRatio = StationComparator.byBikesCapacityRatio();
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation)
                .sorted(byBikesRatio).collect(Collectors.toList());
        List<StationUtilityData> qualities = new ArrayList<>();

        int numStations = stations.size() >= 5 ? Math.floorDiv(stations.size(), 5) : stations.size();
        for (int i = 0; i < numStations; i++) {
            Station station = stations.get(i);
            double quality = qualityToRent(station, point);
            qualities.add(new StationUtilityData(station, quality,point));
        }
        Comparator<StationUtilityData> byQuality = (sq1, sq2) -> Double.compare(sq2.getCurrentUtility(), sq1.getCurrentUtility());
        return qualities.stream().sorted(byQuality).map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());

    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        Comparator<Station> bySlotsRatio = StationComparator.bySlotsCapacityRatio();
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation)
                .sorted(bySlotsRatio).collect(Collectors.toList());
        List<StationUtilityData> qualities = new ArrayList<>();

        int numStations = stations.size() >= 9 ? Math.floorDiv(stations.size(), 3) : stations.size();
        for (int i = 0; i < numStations; i++) {
            Station station = stations.get(i);
            double quality = qualityToReturn(station, point);
            qualities.add(new StationUtilityData(station, quality, point));
        }
        Comparator<StationUtilityData> byQuality = (sq1, sq2) -> Double.compare(sq2.getCurrentUtility(), sq1.getCurrentUtility());
        return qualities.stream().sorted(byQuality).map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
    }

    private double qualityToRent(Station station, GeoPoint point) {
        double summation = 0;
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
                .filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());
        if (!stations.isEmpty()) {
            double factor, multiplication;
            for (Station s : stations) {
                factor = (parameters.maxDistanceRecommendation - station.getPosition().distanceTo(s.getPosition())) / parameters.maxDistanceRecommendation;
                multiplication = s.availableBikes() * factor;
                summation += multiplication;
            }
        }
        return summation;
    }

    private double qualityToReturn(Station station, GeoPoint point) {
        double summation = 0;
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
                .filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());
        if (!stations.isEmpty()) {
            double factor, multiplication;
            for (Station s : stations) {
                factor = (parameters.maxDistanceRecommendation - station.getPosition().distanceTo(s.getPosition())) / parameters.maxDistanceRecommendation;
                multiplication = s.availableSlots() * factor;
                summation += multiplication;
            }
        }
        return summation;
    }

}
