package es.urjc.ia.bikesurbanfleets.comparators;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;

public class StationComparator {
	
	private GeoPoint point;
	
	private Comparator<StationInfo> byDistance;
	private Comparator<StationInfo> byAvailableBikes;
	private Comparator<StationInfo> byAvailableSlots;
	private Comparator<StationInfo> byDistanceBikesRatio;
	private Comparator<StationInfo> byDistanceSlotsRatio;
	private Comparator<StationInfo> byBikesCapacityRatio;
	private Comparator<StationInfo> bySlotsCapacityRatio;
	
	public StationComparator() {
		byDistance = (s1, s2) -> Double.compare(s1.getPosition().distanceTo(this.point), s2
				.getPosition().distanceTo(this.point));
		byAvailableBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes()); 
		byAvailableSlots = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
		byDistanceBikesRatio = (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableBikes(), s2.getPosition().distanceTo(point)/s2.availableBikes());
		byDistanceSlotsRatio =  (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableSlots(), s2.getPosition().distanceTo(point)/s2.availableSlots());
		byBikesCapacityRatio = (s1, s2) -> Double.compare((double)s2.availableBikes()/(double)s2
    			.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
		bySlotsCapacityRatio = (s1, s2) -> Double.compare((double)s2.availableSlots()/(double)s2
    			.getCapacity(), (double)s1.availableSlots()/(double)s1.getCapacity());
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
		return byDistanceBikesRatio;
	}

	public Comparator<StationInfo> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		this.point = point;
		return byDistanceSlotsRatio;
	}

	public Comparator<StationInfo> byBikesCapacityRatio() {
		return byBikesCapacityRatio;
	}

	public Comparator<StationInfo> bySlotsCapacityRatio() {
		return bySlotsCapacityRatio;
	}
		
}
