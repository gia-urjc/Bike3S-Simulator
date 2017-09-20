package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserWantsToLeaveBike extends EventUser {
	
	public EventUserWantsToLeaveBike(int instant, Person user, Station station) {
		super(instant, user, station);
	}

		
		public List<Event> execute() {
			List<Event> events = new ArrayList<Event>();
			int time = getInstant()+(getUser().timeTo(getStation().getPosition()));
			events.add(new EventUserArrivesAtStationToLeaveBike(time, getUser(), getStation()));

			return events;
		}

}