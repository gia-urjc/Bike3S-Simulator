package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class EventUserDecidesReserveSlotOrReturnBike extends Event {
	
	private Person user;
	
	public EventUserDecidesReserveSlotOrReturnBike(int instant, Person user) {
		super(instant);
		this.user = user;
	}

	@Override
	public List<Event> execute() {
		List<Event> newEvents = new ArrayList<>();
		
		Station destination = user.determineStation(); //overloaded method;
		user.setDestinationStation(destination);
		int arrivalTime = user.timeToReach(destination.getPosition());
		
        
        if (user.decidesToReserveSlot()) {
        	boolean reserved = user.reservesSlot(destination);
                   	
            if (reserved) {  // User can reserve
            	if (SystemInfo.reservationTime < arrivalTime) {
            		user.cancelsSlotReservation(destination);
            		newEvents.add(new EventSlotReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            	}
            	else {
            	    newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
            	}
            }
            else {  // user can't reserve
            	if (!user.decidesToLeaveSystem()) {
            		if (!user.decidesToDetermineOtherStation()) {
            			newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
            		}
            		else {
            			user.getStationsReservationAttemps().add(destination);
            			newEvents.add(new EventUserDecidesReserveSlotOrReturnBike(this.getInstant(), user));
            		}	
            	}
            	
            }
          
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
        }
        
        return newEvents;
	}
	
	

}
