package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import java.util.List;
import java.util.ArrayList;

public class EventUserDecidesReserveOrRent extends Event {
	
	public EventUserDecidesReserveOrRent(int instant, Person user) {
		super(instant);
		this.user = user;
	}

	private Person user;
	
	
	public List<Event> execute() {
		List<Event> newEvents = new ArrayList<>();
		
		Station destination = user.determineStation(); //overloaded method;
		user.setDestinationStation(destination);
		int arrivalTime = user.timeToReach(destination.getPosition());
		
        
        if (user.decidesToReserveBike()) {
        	boolean reserved = user.reservesBike(destination);
                   	
            if (reserved) {  // User can reserve
            	if (SystemInfo.reservationTime < arrivalTime) {
            		user.cancelsBikeReservation(destination);
            		newEvents.add(new EventBikeReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            	}
            	else {
            	    newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            	}
            }
            else {  // user can't reserve
            	if (!user.decidesToLeaveSystem()) {
            		if (!user.decidesToDetermineOtherStation()) {
            		newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            		}
            		else {
            			user.getStationsReservationAttemps().add(destination);
            			newEvents.add(new EventUserDecidesReserveOrRent(this.getInstant(), user));
            		}	
            	}
            	
            }
          
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;

	}

}
