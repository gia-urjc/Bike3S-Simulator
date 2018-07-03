package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 * 
 * @author IAgroup
 *
 */
public class RecommendationSystemByAvailableResourcesRatio extends RecommendationSystem {

	/**
	 * It is the maximum distance in meters between the recommended stations and the
	 * indicated geographical point.
	 */
	private int maxDistance = 650;

	/**
	 * It indicates the number of stations to consider when choosing one randomly in
	 * recommendation by ratio between available resources and station capacity.
	 */
	private final int N_STATIONS = 5;
	/**
	 * It contains several comparators to sort stations.
	 */
	private StationComparator stationComparator;

	public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager,
			StationComparator stationComparator) {
		super(infraestructureManager);
		this.stationComparator = stationComparator;
	}

	public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager,
			Integer maxDistance, StationComparator stationComparator) {
		super(infraestructureManager);
		this.maxDistance = maxDistance;
		this.stationComparator = stationComparator;
	}

	private List<Station> fartherStations(GeoPoint point, List<Station> stations) {
		return stations.stream().filter(station -> station.getPosition().distanceTo(point) > maxDistance)
				.collect(Collectors.toList());
	}

	private List<Station> nearerStations(GeoPoint point, List<Station> stations) {
		return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= maxDistance)
				.collect(Collectors.toList());
	}

	private List<Station> rebalanceWhenRenting(List<Station> stations) {
		double ratioSum = 0.0;
		int i;
		int n_stations = stations.size() > N_STATIONS ? N_STATIONS : stations.size();
		for (i = 0; i < n_stations; i++) {
			ratioSum += stations.get(i).availableBikes() / stations.get(i).getCapacity();
		}

		double random = infraestructureManager.getRandom().nextDouble(0, ratioSum);
		double ratio;
		for (i = 0; i < n_stations; i++) {
			ratio = stations.get(i).availableBikes() / stations.get(i).getCapacity();
			if (random <= ratio) {
				break;
			}
			random -= ratio;
		}
		Station selected = stations.remove(i);
		stations.add(0, selected);
		return stations;
	}

	private List<Station> rebalanceWhenReturning(List<Station> stations) {
		double ratioSum = 0.0;
		int i;
		int n_stations = stations.size() > N_STATIONS ? N_STATIONS : stations.size();
		for (i = 0; i < n_stations; i++) {
			ratioSum += stations.get(i).availableSlots() / stations.get(i).getCapacity();
		}

		double random = infraestructureManager.getRandom().nextDouble(0, ratioSum);
		double ratio;
		for (i = 0; i < n_stations; i++) {
			ratio = stations.get(i).availableSlots() / stations.get(i).getCapacity();
			if (random <= ratio) {
				break;
			}
			random -= ratio;
		}
		Station selected = stations.remove(i);
		stations.add(0, selected);
		return stations;
	}

	@Override
	public List<Station> recommendStationToRentBike(GeoPoint point) {
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations());
		List<Station> result;
		if (!stations.isEmpty()) {
		List<Station> nearer = nearerStations(point, stations);
		List<Station> farther = fartherStations(point, stations);
		Comparator<Station> byBikesRatio = stationComparator.byBikesCapacityRatio();
		nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
		farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());

		nearer.addAll(farther);
		result = rebalanceWhenRenting(nearer);
		}
		else {
			result = new ArrayList<>();
		}
		return result;
	}

	public List<Station> recommendStationToReturnBike(GeoPoint point) {
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations());
		List<Station> result;
		if (!stations.isEmpty()) {
		List<Station> nearer = nearerStations(point, stations);
		List<Station> farther = fartherStations(point, stations);
/**		if (stations.size() == 0) {
			nearer = nearerStations(point, infraestructureManager.consultStations());
			if (nearer.size() == 0) {
				farther = fartherStations(point, infraestructureManager.consultStations());
				stations = farther;
			} else {
				stations = nearer;
			}
		} */
		Comparator<Station> bySlotsRatio = stationComparator.bySlotsCapacityRatio();
		nearer = nearer.stream().sorted(bySlotsRatio).collect(Collectors.toList());
		farther = farther.stream().sorted(bySlotsRatio).collect(Collectors.toList());

		nearer.addAll(farther);
		result = rebalanceWhenReturning(nearer);
		}
		else {
			result = new ArrayList<>();
		}
		return result;
	}

}