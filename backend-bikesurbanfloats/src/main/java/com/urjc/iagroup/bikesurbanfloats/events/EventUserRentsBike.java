package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserRentsBike extends EventUser {


	public EventUserRentsBike(int instant, Person user, Station station) {
		super(instant, user, station);
	}


	public List<Event> execute() {
		getUser().removeBikeFrom(getStation());
		List<Event> events = new ArrayList<Event>();
		events.add(new EventUserWantsToLeaveBike(getInstant()+1, getUser(), getStation()));
		return events; 
		
	}

}
