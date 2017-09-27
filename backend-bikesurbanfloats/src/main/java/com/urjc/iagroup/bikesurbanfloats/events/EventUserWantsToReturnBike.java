package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

public class EventUserWantsToReturnBike extends Event {
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

		Station destination = user.determineStation();
		int arrivalTime = user.timeToReach(destination.getPosition());
		
		if ( (user.decidesToReserveSlot(destination)) && (ConfigInfo.reservationTime < arrivalTime) ) {
			user.cancelsSlotReservation(destination);
			newEvents.add(new(getInstant() + arrivalTime, user));
		}
		else
			newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
		return newEvents;
		
	}
	
}
