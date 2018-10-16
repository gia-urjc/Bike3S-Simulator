package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.SimpleRandom;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystemType;
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
@RecommendationSystemType("AVAILABLE_RESOURCES_RATIO")
public class RecommendationSystemByAvailableResourcesRatio extends RecommendationSystem {

	@RecommendationSystemParameters
	public class RecommendationParameters {

		/**
		 * It is the maximum distance in meters between the recommended stations and the
		 * indicated geographical point.
		 */
		private int maxDistance = 800;

	}

	private RecommendationParameters parameters;

	/**
	 * It indicates the number of stations to consider when choosing one randomly in
	 * recommendation by ratio between available resources and station capacity.
	 */
	private final int N_STATIONS = 5;
	/**
	 * It contains several comparators to sort stations.
	 */
	private StationComparator stationComparator;
        private SimpleRandom rand;

	public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager,
			StationComparator stationComparator) {
		super(infraestructureManager);
		this.parameters = new RecommendationParameters();
		this.stationComparator = stationComparator;
                this.rand=new SimpleRandom(1);
	}

	public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager, StationComparator stationComparator,
														 RecommendationParameters parameters) {
		super(infraestructureManager);
		this.parameters = parameters;
		this.stationComparator = stationComparator;
	}

	private List<Station> fartherStations(GeoPoint point, List<Station> stations) {
		return stations.stream().filter(station -> station.getPosition().distanceTo(point) > parameters.maxDistance)
				.collect(Collectors.toList());
	}

	private List<Station> nearerStations(GeoPoint point, List<Station> stations) {
		return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
				.collect(Collectors.toList());
	}

	private List<Station> rebalanceWhenRenting(List<Station> stations) {
		double ratioSum = 0.0;
		int i;
		int n_stations = stations.size() > N_STATIONS ? N_STATIONS : stations.size();
		for (i = 0; i < n_stations; i++) {
			ratioSum += stations.get(i).availableBikes() / stations.get(i).getCapacity();
		}

		double random = rand.nextDouble(0, ratioSum);
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

		double random = rand.nextDouble(0, ratioSum);
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
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations());
		List<Station> temp;
		List<Recommendation> result = new ArrayList<>();
		if (!stations.isEmpty()) {
		List<Station> nearer = nearerStations(point, stations);
		List<Station> farther = fartherStations(point, stations);
		Comparator<Station> byBikesRatio = stationComparator.byBikesCapacityRatio();
		nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
		farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());

		nearer.addAll(farther);
		temp = rebalanceWhenRenting(nearer);
result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
		}
		return result;
	}

	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations());
		List<Station> temp;
		List<Recommendation> result = new ArrayList<>();
		if (!stations.isEmpty()) {
		List<Station> nearer = nearerStations(point, stations);
		List<Station> farther = fartherStations(point, stations);
		Comparator<Station> bySlotsRatio = stationComparator.bySlotsCapacityRatio();
		nearer = nearer.stream().sorted(bySlotsRatio).collect(Collectors.toList());
		farther = farther.stream().sorted(bySlotsRatio).collect(Collectors.toList());
		nearer.addAll(farther);
		temp = rebalanceWhenReturning(nearer);
		result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
		}
		
		return result;
	}

}