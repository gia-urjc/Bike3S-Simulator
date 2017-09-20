package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserLeavesBike extends EventUser {
	
	public EventUserLeavesBike(int instant, Person user, Station station) {
		super(instant, user, station);
	}
	
	public List<Event> execute() {
		getUser().returnBikeTo(getStation());
		List<Event> events = new ArrayList<Event>();
		return events;
	}

}