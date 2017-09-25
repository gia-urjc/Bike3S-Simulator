package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserWantsToReturnBike extends Event{

	private Person user;
	
	public EventUserWantsToReturnBike(int instant, Person user) {
		super(instant);
		this.user = user;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}
	
	public List<Event> execute() {
		List<Event> newEvents = new ArrayList<Event>();
		Station decision = user.determineDestination();
		int arrivalTime = user.timeToReach(decision.getPosition());
		newEvents.add(new EventUserArrivesAtStationToReturnBike(arrivalTime, user, decision));
		return newEvents;
		
	}
	
}
