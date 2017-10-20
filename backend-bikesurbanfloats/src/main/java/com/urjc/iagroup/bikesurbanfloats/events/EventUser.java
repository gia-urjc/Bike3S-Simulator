package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

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
		
		Station destination = user.determineStationToRentBike(instant); 
		user.setDestinationStation(destination);
		int arrivalTime = user.timeToReach(destination.getPosition());
		
        if (user.decidesToReserveBike()) {
        	Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination);
        	boolean reserved = user.reservesBike(destination);
                   	
            if (reserved) {  // User has been able to reserve
            	reservation.setSuccessful(true);
            	if (SystemInfo.reservationTime < arrivalTime) {
            		reservation.setTimeout(true);
            		user.addReservation(reservation);
            		user.cancelsBikeReservation(destination);
            		newEvents.add(new EventBikeReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            	}
            	else {
            		user.addReservation(reservation);
            	    newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            	}
            }
            else {  // user hasn't been able to reserve
            	user.addReservation(reservation);
            	if (!user.decidesToLeaveSystem()) {
            		
            		if (!user.decidesToDetermineOtherStation()) {  // user walks to the initially chosen station
            		newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            		}
            		else {
            	  			newEvents = manageBikeReservationDecision();
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
		
		Station destination = user.determineStationToReturnBike(instant); 
		user.setDestinationStation(destination);
		int arrivalTime = user.timeToReach(destination.getPosition());
        
        if (user.decidesToReserveSlot()) {
        	Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination);
        	boolean reserved = user.reservesSlot(destination);

         if (reserved) {  // User has been able to reserve
        	 reservation.setSuccessful(true);
        	 
            	if (SystemInfo.reservationTime < arrivalTime) {
            		reservation.setTimeout(true);
            		user.addReservation(reservation);
            		user.cancelsSlotReservation(destination);
            		newEvents.add(new EventSlotReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            	}
            	else {
            		user.addReservation(reservation);
            	    newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
            	}
            }
            else {  // user hasn't been able to reserve
            	user.addReservation(reservation);
        		if (!user.decidesToDetermineOtherStation()) {  // user waljs to the initially chosen station 
        			newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
        		}
        		else {
        			newEvents = manageSlotReservationDecision();
        		}	
        	}
        }
	    else {   // user decides not to reserve
				newEvents.add(new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, destination));
	    }
        return newEvents;
    }
}