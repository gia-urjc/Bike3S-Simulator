package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;

public class EventUserWantsToLeaveBike extends Event{

	private Person user;
	
	public EventUserWantsToLeaveBike(int instant, Person user) {
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
		List<Event> newEvents = new ArrayList<Evet>();
		Station decision = user.determineDestination();
		int arrivalTime = user.timToReach(decision.getPosition());
		newEvents.add(new EventUserArrivesAtStationToLeaveBike(arrivalTime, user, decision));
		return newEvents;
		
	}
	
}
