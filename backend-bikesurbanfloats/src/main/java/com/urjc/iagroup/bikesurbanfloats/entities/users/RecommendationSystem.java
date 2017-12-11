package com.urjc.iagroup.bikesurbanfloats.entities.users;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;


/** 
 * This class is a system which recommends the user the stations that, with respect to a 
 * geographical point, are less than a certain preset distance.
 * The geographical point may be the user position, if he wants to rent or to return a bike 
 * in the closest station to himself, or a place he is going to reach, if he wants to return 
 * the bike in the closest station to that place.    
 * Then, this system gives the user a list of stations ordered ascending or descending (depending 
 *    
 * @author IAgroup
 *
 */
public class RecommendationSystem {
	
	/**
	 * It is the maximum distance in meters between the recommended stations and the indicated 
	 * geographical point.
	 */
	private final int MAX_DISTANCE = 1500;
	
	/**
	 * It verifies which stations are less than MAX_DISTANCE meters from the indicated 
	 * geographical point. 
	 * @param point It's the user current position or the geographical coordinates of a 
	 * place the user wants to reach.
	 * @param stations It's the initial list of stations within it has to filter those 
	 * that don't exceed the preset maximum distance.      
	 * @return an unordered list of stations from which the system will prepare its recommendations.
	 */
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