package es.urjc.ia.bikesurbanfleets.comparators;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class StationComparator {
	
	public StationComparator() {}

	public Comparator<Station> byDistance(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
	}

	public Comparator<Station> byAvailableBikes() {
		return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
	}

	public Comparator<Station> byAvailableSlots() {
		return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
	}

	public Comparator<Station> byProportionBetweenDistanceAndBikes(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableBikes(), s2.getPosition().distanceTo(point)/s2.availableBikes());
	}

	public Comparator<Station> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableSlots(), s2.getPosition().distanceTo(point)/s2.availableSlots());
	}

	public Comparator<Station> byBikesCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableBikes()/(double)s2
				.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
	}

	public Comparator<Station> bySlotsCapacityRatio() {
		return (s1, s2) -> Double.compare((double)s2.availableSlots()/(double)s2
				.getCapacity(), (double)s1.availableSlots()/(double)s1.getCapacity());
	}
		
}
