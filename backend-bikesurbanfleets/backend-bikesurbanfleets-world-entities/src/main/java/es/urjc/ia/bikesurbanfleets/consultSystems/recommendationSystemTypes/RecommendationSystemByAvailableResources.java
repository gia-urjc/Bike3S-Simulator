package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
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
@RecommendationSystemType("AVAILABLE_RESOURCES")
public class RecommendationSystemByAvailableResources extends RecommendationSystem {

	@RecommendationSystemParameters
	public class RecommendationParameters {

		/**
		 * It is the maximum distance in meters between the recommended stations and the
		 * indicated geographical point.
		 */
		private int maxDistance = 650;

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

	public RecommendationSystemByAvailableResources(InfraestructureManager infraestructureManager,
			StationComparator stationComparator) {
		super(infraestructureManager);
		this.parameters = new RecommendationParameters();
		this.stationComparator = stationComparator;
	}

	public RecommendationSystemByAvailableResources(InfraestructureManager infraestructureManager, StationComparator stationComparator,
														 RecommendationParameters parameters) {
		super(infraestructureManager);
		this.parameters = parameters;
		this.stationComparator = stationComparator;
	}


	@Override
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		List<Station> temp;
		List<Recommendation> result = new ArrayList<>();
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
				.filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		
		if (!stations.isEmpty()) {
		Comparator<Station> byBikes = stationComparator.byAvailableBikes();
		temp = stations.stream().sorted(byBikes).collect(Collectors.toList());
		result = temp.stream().map(station -> new Recommendation(station, 0.0)).collect(Collectors.toList());
	}
		return result;
	}

	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		List<Station> temp;
		List<Recommendation> result = new ArrayList<>();
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream().filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistance).collect(Collectors.toList());
		
		if (!stations.isEmpty()) {
		Comparator<Station> bySlots = stationComparator.byAvailableSlots();
		temp = stations.stream().sorted(bySlots).collect(Collectors.toList());
		result = temp.stream().map(s -> new Recommendation(s, 0.0)).collect(Collectors.toList());
		}

		return result;
	}

}