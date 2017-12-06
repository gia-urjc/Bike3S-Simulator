package com.urjc.iagroup.bikesurbanfloats.entities.users.recommendations;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;

public class RecommendationSystem {
	private final int MAX_DISTANCE = 1500; 
	private List<Station> stations;
	private GraphManager graph;
	
	private Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s1.availableBikes(), s2.availableBikes());
	private Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s1.availableSlots(), s2.availableSlots());

	
	public RecommendationSystem(List<Station> stations, GraphManager graph) {
		super();
		this.stations = stations;
		this.graph = graph;
	}
	
	private List<Station> validStations(User user) {
		List<Station> validStations = new ArrayList<>();
		
		for (Station station: stations) {
			if (station.getPosition().distanceTo(user.getPosition()) <= MAX_DISTANCE) {
				validStations.add(station);
			}
		}
		return validStations;
	}

	public List<Station> recommendByNumberOfBikes(User user) {
		return validStations(user).stream().sorted(byNumberOfBikes).collect(Collectors.toList());
	}
	
	public List<Station> recommendByNumberOfSlots(User user) {
		List<Station> recommendedStations = validStations(user);
		return validStations(user).stream().sorted(byNumberOfSlots).collect(Collectors.toList());
	}

public List<Station> recommendByLinearDistance(User user) {
	Comparator<Station> byLinearDistance = (s1, s2) -> Double.compare(s1.getPosition()
			.distanceTo(user.getPosition()), s2.getPosition().distanceTo(user.getPosition()));
	return validStations(user).stream().sorted(byLinearDistance).collect(Collectors.toList());
}

}
