package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public abstract class EventUser implements Event {
	protected int instant;
	protected Person user;

    public EventUser(int instant, Person user) {
        this.instant = instant;
        this.user = user;
    }
    
    public int getInstant() {
    	return instant;
    }
    
    public Person getUser() {
    	return user;
    }
    
    public int compareTo(Event event) {
        return Integer.compare(this.instant, event.getInstant());
    }
    
    public String toString() {
    	return "Event: "+getClass().getSimpleName()+"\nInstant: "+instant+"\n"+"User: "+user.toString()+"\n";
    }
    
    public abstract List<Event> execute();
    
    public List<Event> manageBikeReservationDecision() {
List<Event> newEvents = new ArrayList<>();
		
		Station destination = user.determineStation(); //overloaded method;
		user.setDestinationStation(destination);
		// TODO: user musts register chosen stations in which rent a bike
		
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
            			manageBikeReservationDecision();
            		}	
            	}
            }
          
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    public List<Event> manageSlotReservationDecision() {
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
            			newEvents = manageSlotReservationDecision();
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