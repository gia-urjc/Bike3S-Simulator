package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;

public class EventUserWantsToRentBike extends EventUser {


	public List<Event> execute() {
		List<Event> events=new ArrayList<Event>;
		user.setLocation(station.getLocation());
		events.add(new EventUserArrivesStation(getInstant()+time, user, station));
		return events;
	}
}