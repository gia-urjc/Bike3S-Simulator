package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserArrivesAtStationToRentBike extends EventUser {
	private int time;
	
	public EventUserArrivesAtStationToRentBike 
(int instant, Person user, Station station, int time) {
		super(instant, user, station);
		this.time = time;
	}
	public List<Event> execute() {
		getUser().setPosition(getStation().getPosition());
		List<Event> events = new ArrayList<Event>();
		if (getStation().availableBikes() > 0) {
					events.add(new EventUserRentsBike(getInstant()+1, getUser(), getStation()));
					return events;
		}
		else {
			//if there aren't bikes, user decides to go to another station or to leave the system
			Station decision = getUser().determineDestination();
			if (decision != null) {   //if user decides to go to another station
				events.add(new EventUserWantsToRentBike(getInstant()+1, getUser(), decision));
				return events;
			}
			else   //user leaves the system
				return events;
		}
	}
}