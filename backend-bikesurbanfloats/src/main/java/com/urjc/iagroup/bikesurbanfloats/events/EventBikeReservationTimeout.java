package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.*;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import java.util.List;
import java.util.ArrayList;

public class EventBikeReservationTimeout extends Event {
		private Person user;
	
	public EventBikeReservationTimeout(int instant, Person user) {
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
		
	       if (destination != null) {  // user doesn`t want to leave the system
	        	int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());
	        
	        	if ( (user.decidesToReserveBike(destination)) && (ConfigInfo.reservationTime < arrivalTime) ) {
	        		user.cancelsBikeReservation(destination);
	       			newEvents.add(new EventBikeReservationTimeout(this.getInstant() + ConfigInfo.reservationTime, user ));
	        	}
	        	else
	        		newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
	        }
	 
		return newEvents;
		
	}
	
}