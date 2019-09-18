package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

public class StationComparator {
	
	public StationComparator() {}

	public static Comparator<Station> byDistance(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
	}

	public static Comparator<Station> byAvailableBikes() {
		return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
	}

	public static Comparator<Station> byAvailableSlots() {
		return (s1, s2) -> Integer.compare(s2.availableSlots(), s1.availableSlots());
	}

	public static Comparator<Station> byProportionBetweenDistanceAndBikes(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    (double) s1.availableBikes(),
                                                  s2.getPosition().distanceTo(point)/
                                                    (double) s2.availableBikes());
	}

	public static Comparator<Station> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    (double) s1.availableSlots(),
                                                  s2.getPosition().distanceTo(point)/
                                                    (double) s2.availableSlots());
	}

	public static Comparator<Station> byProportionBetweenDistanceAndBikeRatio(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    ((double)s1.availableBikes()/(double)s1.getCapacity()),
                                                  s2.getPosition().distanceTo(point)/
                                                    ((double)s2.availableBikes()/(double)s2.getCapacity()));
	}

	public static Comparator<Station> byProportionBetweenDistanceAndSlotRatio(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    ((double)s1.availableSlots()/(double)s1.getCapacity()),
                                                  s2.getPosition().distanceTo(point)/
                                                    ((double)s2.availableSlots()/(double)s2.getCapacity()));
	}

	public static Comparator<Station> byBikesCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableBikes()/(double)s2
				.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
	}

	public static Comparator<Station> bySlotsCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableSlots()/(double)s2
				.getCapacity(), (double)s1.availableSlots()/(double)s1.getCapacity());
	}
		
}
