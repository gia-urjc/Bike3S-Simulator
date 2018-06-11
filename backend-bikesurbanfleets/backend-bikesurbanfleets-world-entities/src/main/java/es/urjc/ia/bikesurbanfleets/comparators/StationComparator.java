package es.urjc.ia.bikesurbanfleets.comparators;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;

public class StationComparator {
	
	public StationComparator() {}

	public Comparator<StationInfo> byDistance(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
	}

	public Comparator<StationInfo> byAvailableBikes() {
		return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
	}

	public Comparator<StationInfo> byAvailableSlots() {
		return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
	}

	public Comparator<StationInfo> byProportionBetweenDistanceAndBikes(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableBikes(), s2.getPosition().distanceTo(point)/s2.availableBikes());
	}

	public Comparator<StationInfo> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableSlots(), s2.getPosition().distanceTo(point)/s2.availableSlots());
	}

	public Comparator<StationInfo> byBikesCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableBikes()/(double)s2
				.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
	}

	public Comparator<StationInfo> bySlotsCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableSlots()/(double)s2
				.getCapacity(), (double)s1.availableSlots()/(double)s1.getCapacity());
	}
		
}
