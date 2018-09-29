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
public class RecommendationSystemWithEconomicIncentives extends RecommendationSystem {
	
	@RecommendationSystemParameters
	public class RecommendationParameters {
		private int maxDistance = 750;
	}
	
	private final int INCENTIVE = 100;   // 1 euro per 100 meters
	private RecommendationParameters parameters;
	private StationComparator stationComparator;
	private List<Station> selectedStations;
	
	public RecommendationSystemWithEconomicIncentives(InfraestructureManager infraestructure, StationComparator comparator) {
		super(infraestructure);
		this.parameters = new RecommendationParameters();
		this.stationComparator = comparator;
		this.selectedStations = new ArrayList<>();
	}
		
	public RecommendationSystemWithEconomicIncentives(InfraestructureManager infraestructure, StationComparator comparator, 
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
	
	public double calculateIncentive(GeoPoint point, Station station) {
		Comparator<Station> byDistance = stationComparator.byDistance(point);
		List<Station> stations = infraestructureManager.consultStations().stream()
				.filter(s -> s.getPosition().distanceTo(point) <= parameters.maxDistance)
				.sorted(byDistance).collect(Collectors.toList());
		Station nearerStation = stations.get(0);
		double idealDistance = nearerStation.getPosition().distanceTo(point);
		double distance = station.getPosition().distanceTo(point);
		return (distance - idealDistance)/INCENTIVE;
	}

}