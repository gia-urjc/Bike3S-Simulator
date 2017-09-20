package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesStationToRentBike extends EventUser {
	private int time;

	public List<Event> execute() {
	List<Event> events=new ArrayList<Event>();
	if (getStation().getBikes()>0) {
		events.add(new EventUserRentsBike(getInstant()+time, user, station));
		return evetns;
	}
	else {
		//if there aren't bikes, user decides to go to another station or to leave the system
		Station decision=user.decides();
		if (decision!=null) {   //if user decides to go to another station
			int newTime = user.getLocation().distance(decision.getLocation());   //time that user takes in arriving at the new station
			events.add(new EventUserWantsToRentBike(getInstant()+1, user, decision, newTime));
		}
		else   //user leaves the system
			return events;
	}
}