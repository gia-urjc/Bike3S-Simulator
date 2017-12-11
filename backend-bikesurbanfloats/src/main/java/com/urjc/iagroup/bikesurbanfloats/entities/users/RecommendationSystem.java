package com.urjc.iagroup.bikesurbanfloats.entities.users;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public class RecommendationSystem {
	private final int MAX_DISTANCE = 1500;
	
	private List<Station> validStations(GeoPoint point, List<Station> stations) {
		return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= MAX_DISTANCE)
				.collect(Collectors.toList());
	}

	public List<Station> recommendByNumberOfBikes(GeoPoint point, List<Station> stations) {
		Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s1.availableBikes(), s2.availableBikes());
		return validStations(point, stations).stream().sorted(byNumberOfBikes).collect(Collectors.toList());
	}

	public List<Station> recommendByNumberOfSlots(GeoPoint point, List<Station> stations) {
		Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s1.availableSlots(), s2.availableSlots());
		return validStations(point, stations).stream().sorted(byNumberOfSlots).collect(Collectors.toList());
	}

	public List<Station> recommendByLinearDistance(GeoPoint point, List<Station> stations) {
		Comparator<Station> byLinearDistance = (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point),
				s2.getPosition().distanceTo(point));
		return validStations(point, stations).stream().sorted(byLinearDistance).collect(Collectors.toList());
	}

	public List<Station> recommendByProportionBetweenDistanceAndBikes(GeoPoint point, List<Station> stations) {
		Comparator<Station> byProportion = (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableBikes(), s2.getPosition().distanceTo(point)/s2.availableBikes());
		return validStations(point, stations).stream().sorted(byProportion).collect(Collectors.toList());
	}
	
	public List<Station> recommendByProportionBetweenDistanceAndSlots(GeoPoint point, List<Station> stations) {
		Comparator<Station> byProportion = (s1, s2) -> Double.compare(s1.getPosition()
				.distanceTo(point)/s1.availableSlots(), s2.getPosition().distanceTo(point)/s2.availableSlots());
		return validStations(point, stations).stream().sorted(byProportion)
				.collect(Collectors.toList());
	}
	
	// TODO: byRealRouteDistance
}