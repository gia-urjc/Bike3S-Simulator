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

@RecommendationSystemType("ECONOMIC_INCENTIVES")
public class RecommendationSystemByStationSetsWithIncentives extends RecommendationSystem {
	
	@RecommendationSystemParameters
	public class RecommendationParameters {
		private int maxDistance = 700;
	}
	
	private int COMPENSATION = 10;   // 1 cent of dicount per 10 meters extra to walk 
	private	int EXTRA = 3;   // 3 cents of discount per each extra bresource     
	private RecommendationParameters parameters;
	private StationComparator stationComparator;
	private List<Station> selectedStations;
	
	public RecommendationSystemByStationSetsWithIncentives(InfraestructureManager infraestructure, StationComparator comparator) {
		super(infraestructure);
		this.parameters = new RecommendationParameters();
		this.stationComparator = comparator;
		this.selectedStations = new ArrayList<>();
	}
		
	public RecommendationSystemByStationSetsWithIncentives(InfraestructureManager infraestructure, StationComparator comparator, 
			RecommendationParameters parameters) {
		super(infraestructure);
		this.parameters = parameters;
		this.stationComparator = comparator;
		this.selectedStations = new ArrayList<>();
	}
		
	@Override
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		List<Recommendation> result;  
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		
		if (!stations.isEmpty()) {
			Comparator<Station> byBikeRatio = stationComparator.byBikesCapacityRatio();
			stations = stations.stream().sorted(byBikeRatio).collect(Collectors.toList());
		
			if (stations.size() >= 9) {
				List<StationQuality> qualities = new ArrayList();
				int numSets = stations.size() / 3;
				
			for (int i=0; i<numSets; i++) {
						Station station = stations.get(i);
						StationQuality quality = qualityToRent(station);
						qualities.add(quality);
		 }
			Station nearestStation = getNearestStationToRent(point);
			result = qualities.stream().map(sq -> { 
				Station station = sq.getStation();
				double incentive = calculateIncentiveToRent(point, nearestStation, station);
				Recommendation recommendation = new Recommendation(station, incentive);
				return recommendation;
			}).collect(Collectors.toList());
			}
			else {
				List<Station> temp = new ArrayList<>();
				for (int i=0; i<stations.size(); i++) {
					temp.add(stations.get(i));
				}
				Station nearestStation = getNearestStationToRent(point);
				result = temp.stream().map(s -> {
					double incentive = calculateIncentiveToReturn(point, nearestStation, s);
					return new Recommendation(s, incentive);
				}).collect(Collectors.toList());
			}
		}
		else {
			result = new ArrayList();
		}
		return result;
	}
	
	@Override
	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		List<Recommendation> result;  
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		
		if (!stations.isEmpty()) {
			Comparator<Station> bySlotRatio = stationComparator.bySlotsCapacityRatio();
			stations = stations.stream().sorted(bySlotRatio).collect(Collectors.toList());
		
			if (stations.size() >= 9) {
				List<StationQuality> qualities = new ArrayList();
				int numSets = stations.size() / 3;
				
			for (int i=0; i<numSets; i++) {
						Station station = stations.get(i);
						StationQuality quality = qualityToReturn(station);
						qualities.add(quality);
		 }
			result = qualities.stream().map(sq -> { 
				Station station = sq.getStation();
				double incentive = calculateIncentive(point, station);
				Recommendation recommendation = new Recommendation(station, incentive);
				return recommendation;
			}).collect(Collectors.toList());
			}
			else {
				List<Station> temp = new ArrayList<>();
				for (int i=0; i<stations.size(); i++) {
					temp.add(stations.get(i));
				}
				result = temp.stream().map(s -> {
					double incentive = calculateIncentive(point, s);
					return new Recommendation(s, incentive);
				}).collect(Collectors.toList());
			}
		}
		else {
			result = new ArrayList();
		}
		return result;
	}
	
	private StationQuality qualityToRent(Station station) {
		Comparator<Station> byDistance = stationComparator.byDistance(station.getPosition());
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.sorted(byDistance).collect(Collectors.toList());
		if (stations.get(0).getId() == station.getId()) {
			stations.remove(0);
		}
		
		List<Station> stationSet = new ArrayList();
		stationSet.add(station);
		
		for (int i=0; i<2; i++) {
			Station s = stations.get(i);
			if (!selectedStations.contains(s)) {
				stationSet.add(s);
				selectedStations.add(s);
			}
		}
		
		int quality = 0;
		for(Station s: stationSet) {
			quality += s.availableBikes();
		}
		return new StationQuality(station, quality);
	}
	
	private StationQuality qualityToReturn(Station station) {
			Comparator<Station> byDistance = stationComparator.byDistance(station.getPosition());
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.sorted(byDistance).collect(Collectors.toList());
		if (stations.get(0).getId() == station.getId()) {
			stations.remove(0);
		}
		
		List<Station> stationSet = new ArrayList();
		stationSet.add(station);
		
		for (int i=0; i<2; i++) {
			Station s = stations.get(i);
			if (!selectedStations.contains(s)) {
				stationSet.add(s);
				selectedStations.add(s);
			}
		}
		
		int quality = 0;
		for(Station s: stationSet) {
			quality += s.availableSlots();
		}
		return new StationQuality(station, quality);
	}
	
	private Station getNearestStationToRent(GeoPoint point) {
		Comparator<Station> byDistance = stationComparator.byDistance(point);
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance)
				.sorted(byDistance).collect(Collectors.toList());
		return stations.get(0);
	}
	
	private Station getNearestStationToReturn(GeoPoint point) {
		Comparator<Station> byDistance = stationComparator.byDistance(point);
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance)
				.sorted(byDistance).collect(Collectors.toList());
		return stations.get(0);
	}
	
	private double extraWhenRenting(Station nearestStation, Station recommendedStation) {
		int nearestStationBikes = narestStation.availableBikes();
		int recommendedStationBikes = recommendedStation.availableBikes();
		return (recommendedStationBikes - nearestStationBikes)*EXTRA;
	}
	
	private double extraWhenReturning(Station nearestStation, Station recommendedStation) {
		int nearestStationSlots = narestStation.availableSlots();
		int recommendedStationSlots = recommendedStation.availableSlots();
		return (recommendedStationSlots - nearestStationSlots)*EXTRA;
	}
	
	private double compensation(GeoPoint point, Station nearestStation, Station recommendedStation) {
		double distanceToNearestStation = nearerStation.getPosition().distanceTo(point);
		double distanceToRecommendedStation = recommendedStation.getPosition().distanceTo(point);
		return (distanceToRecommendedStation - distanceToNearestStation)/COMPENSATION;
	}
	
	public double calculateIncentiveToRent(GeoPoint point, Station nearestStation, Station recommendedStation) {
	double compensation = compensation(point, nearestStation, recommendedStation);
	double extra = extraWhenRenting(nearestStation, recommendedStation);
	return compensation+extra;
	}
	
	public double calculateIncentiveToReturn(GeoPoint point, Station nearestStation, Station recommendedStation) {
	double compensation = compensation(point, nearestStation, recommendedStation);
	double extra = extraWhenReturning(nearestStation, recommendedStation);
	return compensation+extra;
	}
	

}