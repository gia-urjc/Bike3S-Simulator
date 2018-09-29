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


@RecommendationSystemType("SURROUNDING_STATIONS")
public class RecommendationSystemBySurroundingStations extends RecommendationSystem {

	@RecommendationSystemParameters
	public class RecommendationParameters {
		private int maxDistance = 750;
	}
	
	private RecommendationParameters parameters;
	private StationComparator stationComparator;
	
	public RecommendationSystemBySurroundingStations(InfraestructureManager infraestructure, StationComparator stationComparator) {
		super(infraestructure);
		this.parameters = new RecommendationParameters();
		this.stationComparator = stationComparator;
	}
	
	public RecommendationSystemBySurroundingStations(InfraestructureManager infraestructure, StationComparator stationComparator,
			RecommendationParameters parameters) {
		super(infraestructure);
		this.parameters = parameters;
		this.stationComparator = stationComparator;
	}
	
	@Override
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		Comparator<Station> byBikesRatio = stationComparator.byBikesCapacityRatio();
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
				.sorted(byBikesRatio).collect(Collectors.toList());
		List<StationQuality> qualities = new ArrayList();
		
		int numStations = stations.size() >= 5 ? Math.floorDiv(stations.size(), 5) : stations.size();
		for(int i=0; i<numStations; i++) {
			Station station = stations.get(i);
			double quality = qualityToRent(station, point);
			qualities.add(new StationQuality(station, quality));
		}
		Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
		return qualities.stream().sorted(byQuality).map(sq -> new Recommendation(sq.getStation(), -1.0)).collect(Collectors.toList());
		 
	}
	
	@Override
	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		Comparator<Station> bySlotsRatio = stationComparator.bySlotsCapacityRatio();
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
				.sorted(bySlotsRatio).collect(Collectors.toList());
		List<StationQuality> qualities = new ArrayList();

		int numStations = stations.size() >= 9 ? Math.floorDiv(stations.size(), 3) : stations.size();
		for(int i=0; i<numStations; i++) {
			Station station = stations.get(i);
			double quality = qualityToReturn(station, point);
			qualities.add(new StationQuality(station, quality));
		}
		Comparator<StationQuality> byQuality = (sq1, sq2) -> Double.compare(sq2.getQuality(), sq1.getQuality());
		return qualities.stream().sorted(byQuality).map(sq -> new Recommendation(sq.getStation(), -1.0)).collect(Collectors.toList());
	}
	
	private double qualityToRent(Station station, GeoPoint point) {
		double summation = 0;
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		if (!stations.isEmpty()) {
			double factor, multiplication;
			for(Station s: stations) {
				factor = (parameters.maxDistance - station.getPosition().distanceTo(s.getPosition()))/parameters.maxDistance;
				multiplication = s.availableBikes()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}

	private double qualityToReturn(Station station, GeoPoint point) {
		double summation = 0;
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		if (!stations.isEmpty()) {
			double factor, multiplication;
			for(Station s: stations) {
				factor = (parameters.maxDistance - station.getPosition().distanceTo(s.getPosition()))/parameters.maxDistance;
				multiplication = s.availableSlots()*factor;
				summation += multiplication; 
			}
		}
		return summation;
	}
	

}
