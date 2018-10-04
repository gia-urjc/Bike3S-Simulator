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


@RecommendationSystemType("DISTANCE_RESOURCES_RATIO")
public class RecommendationSystemByDistanceResourcesRatio extends RecommendationSystem {
	
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
	 * It contains several comparators to sort stations.
	 */
	private StationComparator stationComparator;

	public RecommendationSystemByDistanceResourcesRatio(InfraestructureManager infraestructureManager,
			StationComparator stationComparator) {
		super(infraestructureManager);
		this.parameters = new RecommendationParameters();
		this.stationComparator = stationComparator;
	}

	public RecommendationSystemByDistanceResourcesRatio(InfraestructureManager infraestructureManager, StationComparator stationComparator,
														 RecommendationParameters parameters) {
		super(infraestructureManager);
		this.parameters = parameters;
		this.stationComparator = stationComparator;
	}
	
	@Override
	public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
		List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations());
	 List<Station> temp;
	 List<Recommendation> result = new ArrayList<>();
		if (!stations.isEmpty()) {
			Comparator<Station> byDistanceBikesRatio = stationComparator.byProportionBetweenDistanceAndBikes(point);
			temp = stations.stream().filter( station -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
					.sorted(byDistanceBikesRatio).collect(Collectors.toList());
			result = temp.stream().map(s -> new Recommendation(s, 0.0)).collect(Collectors.toList());
		}
		return result;	
	}
	
	@Override
	public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
		List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations());
		List<Station> temp;
		List<Recommendation> result = new ArrayList<>();
		if (!stations.isEmpty()) {
			Comparator<Station> byDistanceSlotsRatio = stationComparator.byProportionBetweenDistanceAndSlots(point);
			temp = stations.stream().filter( station  -> station.getPosition().distanceTo(point) <= parameters.maxDistance)
					.sorted(byDistanceSlotsRatio).collect(Collectors.toList());
			result = temp.stream().map(s -> new Recommendation(s, 0.0)).collect(Collectors.toList());
		}
		return result;
}
	
}