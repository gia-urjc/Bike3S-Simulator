package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserArrivesAtStationToLeaveBike extends EventUser {
	
	public EventUserArrivesAtStationToLeaveBike(int instant, Person user, Station station) {
		super(instant, user, station);
	}
	
	public List<Event> execute(){
		List<Event> events = new ArrayList<Event>();
		if (getStation().availableSlots() > 0) {
			events.add(new EventUserLeavesBike(getInstant()+1, getUser(), getStation()));
			return events;
		}
		else {
			Station decision = getUser().determineDestination();
			if (decision != null) {
				events.add(new EventUserWantsToLeaveBike(getInstant()+1, getUser(), decision));
				return events;
			}
		}
	}
	
}