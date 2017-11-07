package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents all events which are related to a user, i. e., posible 
 * actions that a user can do at the sytem and facts derived from his actions.
 * It provides methods to manage reservation decisions and reservations themselves
 * at any event which involves a user and, of course, a method to execute and process the events.
 * @author IAgroup
 *
 */

public abstract class EventUser implements Event {
	/**
	 * It is the time instant when event happens.
	 */
	protected int instant;
	/**
	 * It is the user who is involved in the event.
	 */
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
    	return print();
    }
    
    /**
     * It processes the event so that the relevant changes at the system occur  
     */
    public abstract List<Event> execute() throws Exception;
    
    /**
     * It tries to make the bike reservation:
     * 	<ul>
     *  <li>If it is possible, user may have time to reach the station  while reservation is active or may not.
     * 	<li>If it isn't possible, in case of the user decides not to leave the system,
     * 	he makes a decision: to arrive at chosen station without reservation or 
     * 	to repeat all the process after deciding to reserve at a new chosen station.
     *  </ul>  
     * @param destination: it is the station for which user wants to make a bike reservation.
     *  This parameter can be the previous chosen station or a new decided destination station. 
     * @return a list of events (actually, it returns a unique event) that will occur as a consequence of executing the current event.
     */
    
    public List<Event> manageBikeReservation(Station destination) throws Exception{
        List<Event> newEvents = new ArrayList<>();
        int arrivalTime = user.timeToReach();
        Bike bike = user.reservesBike(destination);
        if (bike != null) {  // user has been able to reserve a bike  
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination, bike);
            if (Reservation.VALID_TIME < arrivalTime) {
            	user.setCurrentRoute(user.reachedRouteUntilTimeOut());
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
    /**
     * It is a recursive method
     * At this method, user decides to try to make again the bike reservation (or not) at previous chosen station  
     * @return
     */
    
    public List<Event> manageBikeReservationDecisionAtSameStationAfterTimeout() throws Exception {
    	List<Event> newEvents = new ArrayList<>();
    	Station destination = user.getDestinationStation();
    	int arrivalTime = user.timeToReach();
    	System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
		
        if (user.decidesToReserveBikeAtSameStationAfterTimeout()) {
            newEvents = manageBikeReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    public List<Event> manageBikeReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToRentBike(instant);
        
        user.setDestinationStation(destination);
        List<GeoRoute> allRoutes = user.calculateRouteStation(destination);
        GeoRoute chosenRoute = user.determineRoute(allRoutes);
        user.setCurrentRoute(chosenRoute);
        
        int arrivalTime = user.timeToReach();
        System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
        
        if (user.decidesToReserveBikeAtNewDecidedStation()) {
            newEvents = manageBikeReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    public List<Event> manageSlotReservation(Station destination) throws Exception{
    	List<Event> newEvents = new ArrayList<>();
    	int arrivalTime = user.timeToReach();
    	if (user.reservesSlot(destination)) {  // User has been able to reserve
       	 Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination, user.getBike());
           	if (Reservation.VALID_TIME < arrivalTime) {
           		user.setCurrentRoute(user.reachedRouteUntilTimeOut());
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

    public List<Event> manageSlotReservationDecisionAtSameStationAfterTimeout() throws Exception {
    	List<Event> newEvents = new ArrayList<>();
		Station destination = user.getDestinationStation(); 
		int arrivalTime = user.timeToReach();
		System.out.println("Destination before user arrival: "+	destination.toString() + " " + user.toString());
        
        if (user.decidesToReserveSlotAtSameStationAfterTimeout()) {
            newEvents = manageSlotReservation(destination);
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
	    }
        return newEvents;
    }
    
    public List<Event> manageSlotReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToReturnBike(instant);
        
        user.setDestinationStation(destination);
        List<GeoRoute> allRoutes = user.calculateRouteStation(destination);
        GeoRoute chosenRoute = user.determineRoute(allRoutes);
        user.setCurrentRoute(chosenRoute);

        int arrivalTime = user.timeToReach();
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