package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

@RecommendationSystemType("SURROUNDING_STATIONS_SIMPLE_INCENTIVES")
public class RecommendationSystemBySurroundingStationsWithSimpleIncentives extends RecommendationSystem {

	@RecommendationSystemParameters
	public class RecommendationParameters {
		private int maxDistance = 700;
	}
	
	private final double COMPENSATION = 10.0; 
	private final double EXTRA = 1.5;
	
	private RecommendationParameters parameters;
	private StationComparator stationComparator;
	
 public RecommendationSystemBySurroundingStationsWithSimpleIncentives(InfraestructureManager infraestructure, StationComparator stationComparator) {
		super(infraestructure);
		this.parameters = new RecommendationParameters();
		this.stationComparator = stationComparator;
	}
	
	public RecommendationSystemBySurroundingStationsWithSimpleIncentives(InfraestructureManager infraestructure, StationComparator stationComparator,
			RecommendationParameters parameters) {
		super(infraestructure);
		this.parameters = parameters;
		this.stationComparator = stationComparator;
	}
	
	@Override
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
				.collect(Collectors.toList());
		List<StationQuality> qualities = new ArrayList<>();
		
		for(int i=0; i<stations.size(); i++) {
			Station station = stations.get(i);
			double quality = qualityToRent(stations, station);
			qualities.add(new StationQuality(station, quality));
		}
		
		Station nearestStation = nearestStationToRent(stations, point);
		
	 Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
	 qualities = qualities.stream().sorted(byQuality).collect(Collectors.toList());
	 
	 List<Recommendation> recommendations = new ArrayList<>();
		int numStations = qualities.size();
		double incentive = 0.0;
		double compensation, extra;
		for (int i=0; i<numStations; i++) {
			Station s = qualities.get(i).getStation();
			if (s.getId() != nearestStation.getId()) {
				compensation = compensation(point, nearestStation, s);
				extra = (numStations - i)*EXTRA; 
				incentive = compensation+extra;
			}
			recommendations.add(new Recommendation(s, incentive));
		}
		return recommendations;
	}
	
	@Override
	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
				.collect(Collectors.toList());
		List<StationQuality> qualities = new ArrayList<>();

		for(int i=0; i<stations.size(); i++) {
			Station station = stations.get(i);
			double quality = qualityToReturn(stations, station);
			qualities.add(new StationQuality(station, quality));
		}
		
		Station nearestStation = nearestStationToReturn(stations, point);

		Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
		qualities = qualities.stream().sorted(byQuality).collect(Collectors.toList());
		
		List<Recommendation> recommendations = new ArrayList<>();
		int numStations = qualities.size();
		double incentive = 0.0;
		double compensation, extra;
		for (int i=0; i<numStations; i++) {
			Station s = qualities.get(i).getStation();
			if (s.getId() != nearestStation.getId()) {
				compensation = compensation(point, nearestStation, s);
				extra = (numStations - i)*EXTRA; 
				incentive = compensation+extra;
			}
			recommendations.add(new Recommendation(s, incentive));
		}
		return recommendations;
	}
	
	private double qualityToRent(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor, multiplication;
			for (Station s: stations) {
				factor = (parameters.maxDistance - station.getPosition().distanceTo(s.getPosition()))/parameters.maxDistance;
				multiplication = s.availableBikes()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}
	
	private double qualityToReturn(List<Station> stations, Station station) {
		double summation = 0;
		if (!stations.isEmpty()) {
			double factor, multiplication;
			for (Station s: stations) {
				factor = (parameters.maxDistance - station.getPosition().distanceTo(s.getPosition()))/parameters.maxDistance;
				multiplication = s.availableSlots()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}

	private Station nearestStationToRent(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = stationComparator.byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableBikes() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}
	
	private Station nearestStationToReturn(List<Station> stations, GeoPoint point) {
		Comparator<Station> byDistance = stationComparator.byDistance(point);
		List<Station> orderedStations = stations.stream().filter(s -> s.availableSlots() > 0)
				.sorted(byDistance).collect(Collectors.toList());
		return orderedStations.get(0);
	}

	private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
		double distanceToNearestStation = nearestStation.getPosition().distanceTo(point);
		double distanceToRecommendedStation = recommendedStation.getPosition().distanceTo(point);
		return (distanceToRecommendedStation - distanceToNearestStation)/COMPENSATION;
	}
	
}
