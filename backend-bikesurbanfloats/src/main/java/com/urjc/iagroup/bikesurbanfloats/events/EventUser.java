package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public abstract class EventUser implements Event {
	protected int instant;
	protected User user;

    public EventUser(int instant, User user) {
        this.instant = instant;
        this.user = user;
    }
    
    public int getInstant() {
    	return instant;
    }
    
    public User getUser() {
    	return user;
    }
    
    public String toString() {
    	return "Event: "+getClass().getSimpleName()+"\nInstant: "+instant+"\n"+"User: "+user.toString()+"\n";
    }
    
    public abstract List<Event> execute();
    
    public List<Event> manageBikeReservation(Station destination){
        List<Event> newEvents = new ArrayList<>();
        int arrivalTime = user.timeToReach(destination.getPosition());
        Bike bike = user.reservesBike(destination);
        if (bike != null) {  // user has been able to reserve a bike  
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination, bike);
            if (Reservation.VALID_TIME < arrivalTime) {
                user.cancelsBikeReservation(destination);
                newEvents.add(new EventBikeReservationTimeout(this.getInstant() + Reservation.VALID_TIME , user, reservation));
            }
            else {
                newEvents.add(new EventUserArrivesAtStationToRentBikeWithReservation(this.getInstant() + arrivalTime, user, destination, reservation));
            }
        }
        else {  // user hasn't been able to reserve a bike
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination);
            user.addReservation(reservation);
            if (!user.decidesToLeaveSystemAffterFailedReservation(instant)) {
                if (!user.decidesToDetermineOtherStationAfterFailedReservation()) {  // user walks to the initially chosen station
                	newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
                }
                else {
                        newEvents = manageBikeReservationDecisionAtOtherStation();
                }   
            }
        }
        return newEvents;
    }
    
    public List<Event> manageBikeReservationDecisionAtSameStationAfterTimeout() {
    	List<Event> newEvents = new ArrayList<>();
    	Station destination = user.getDestinationStation();
    	int arrivalTime = user.timeToReach(destination.getPosition());
    	System.out.println("manageBikeReservationDecisionAtSameStationAfterTimeout");
    	System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
		
        if (user.decidesToReserveBikeAtSameStationAfterTimeout()) {
            newEvents = manageBikeReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    public List<Event> manageBikeReservationDecisionAtOtherStation() {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToRentBike(instant);
        int arrivalTime = user.timeToReach(destination.getPosition());
        user.setDestinationStation(destination);
        System.out.println("manageBikeReservationDecisionAtOtherStation");
        System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
        
        if (user.decidesToReserveBikeAtNewDecidedStation()) {
            newEvents = manageBikeReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    public List<Event> manageSlotReservation(Station destination){
    	List<Event> newEvents = new ArrayList<>();
    	int arrivalTime = user.timeToReach(destination.getPosition());
    	if (user.reservesSlot(destination)) {  // User has been able to reserve
       	 Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination, user.getBike());
           	if (Reservation.VALID_TIME < arrivalTime) {
           		user.cancelsSlotReservation(destination);
           		newEvents.add(new EventSlotReservationTimeout(this.getInstant() + Reservation.VALID_TIME, user, reservation));
           	}
           	else {
           	    newEvents.add(new EventUserArrivesAtStationToReturnBikeWithReservation(this.getInstant() + arrivalTime, user, destination, reservation));
           	}
           }
           else {  // user hasn't been able to reserve a slot
	           	Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination);	
	           	user.addReservation(reservation);
	       		if (!user.decidesToDetermineOtherStationAfterFailedReservation()) {  // user waljs to the initially chosen station 
	       			newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
	       		}
	       		else {
	       			newEvents = manageSlotReservationDecisionAtOtherStation();
	       		}	
       	}
    	return newEvents;
    }

    public List<Event> manageSlotReservationDecisionAtSameStationAfterTimeout() {
    	List<Event> newEvents = new ArrayList<>();
    			Station destination = user.getDestinationStation(); 
    			int arrivalTime = user.timeToReach(destination.getPosition());
    			System.out.println("manageSlotReservationDecisionAtSameStationAfterTimeout");
    			System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
        
        if (user.decidesToReserveSlotAtSameStationAfterTimeout()) {
            newEvents = manageSlotReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
	    }
        return newEvents;
    }
    
    public List<Event> manageSlotReservationDecisionAtOtherStation() {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToReturnBike(instant);
        user.setDestinationStation(destination);
        int arrivalTime = user.timeToReach(destination.getPosition());
        System.out.println("manageSlotReservationDecisionAtOtherStation");
        System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
        
        if (user.decidesToReserveSlotAtNewDecidedStation()) {
            newEvents = manageSlotReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
}