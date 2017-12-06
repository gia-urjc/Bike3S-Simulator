package com.urjc.iagroup.bikesurbanfloats.entities.users.recommendations;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public class RecommendationSystem {
	private final int MAX_DISTANCE = 1500; 
	private List<Station> stations;
	private GraphManager graph;
	private StationComparator comparator;
	
	public RecommendationSystem(List<Station> stations, GraphManager graph, StationComparator comparator) {
		super();
		this.stations = stations;
		this.graph = graph;
		this.comparator = comparator;
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
		return validStations(user).stream().sorted(comparator.byNumberOfBikes).collect(Collectors.toList());
	}
	
	public List<Station> recommendByNumberOfSlots(User user) {
		List<Station> recommendedStations = validStations(user);
		return validStations(user).stream().sorted(comparator.byNumberOfSlots).collect(Collectors.toList());
	}

public List<Station> recommendByLinearDistance() {
	return null;
}
	

}
