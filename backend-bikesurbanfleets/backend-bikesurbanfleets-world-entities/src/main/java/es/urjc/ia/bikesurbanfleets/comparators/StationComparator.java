package es.urjc.ia.bikesurbanfleets.comparators;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;

public class StationComparator {
	
	private GeoPoint point;
	
	private Comparator<StationInfo> byDistance;
	private Comparator<StationInfo> byAvailableBikes;
	private Comparator<StationInfo> byAvailableSlots;
	private Comparator<StationInfo> byProportionBetweenDistanceAndBikes;
	private Comparator<StationInfo> byProportionBetweenDistanceAndSlots;
	
	public StationComparator() {
		byDistance = (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2
				.getPosition().distanceTo(point));
		byAvailableBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes()); 
		byAvailableSlots = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
		byProportionBetweenDistanceAndBikes = (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableBikes(), s2.getPosition().distanceTo(point)/s2.availableBikes());
		byProportionBetweenDistanceAndSlots =  (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableSlots(), s2.getPosition().distanceTo(point)/s2.availableSlots());
	}

	public Comparator<StationInfo> byDistance(GeoPoint point) {
		this.point = point;
		return byDistance;
	}

	public Comparator<StationInfo> byAvailableBikes() {
		return byAvailableBikes;
	}

	public Comparator<StationInfo> byAvailableSlots() {
		return byAvailableSlots;
	}

	public Comparator<StationInfo> byProportionBetweenDistanceAndBikes(GeoPoint point) {
		this.point = point;
		return byProportionBetweenDistanceAndBikes;
	}

	public Comparator<StationInfo> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		this.point = point;
		return byProportionBetweenDistanceAndSlots;
	}
	
	
}
