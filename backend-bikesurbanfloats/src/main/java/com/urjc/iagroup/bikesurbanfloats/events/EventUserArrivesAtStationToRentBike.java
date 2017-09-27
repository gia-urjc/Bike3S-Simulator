package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.*;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToRentBike extends Event {

    private Person user;
    private Station station;

    public EventUserArrivesAtStationToRentBike(int instant, Person user, Station station) {
        super(instant);
        this.user = user;
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        user.setPosition(station.getPosition());

        boolean removedBike = user.removeBikeFrom(station);   // user tries to remove bike
        
        if (removedBike) {
        	
        		if (user.decidesToReturnBike()) {
        	   Station destination = user.determineStation();
            int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());
            
            if ( (user.decidesToReserveSlot(destination)) && (ConfigInfo.reservationTime < arrivalTime) ) {
            	user.cancelsSlotReservation(destination);
            	newEvents.add(new EventSlotReservationTimeout(getInstant() + ConfigInfo.reservationTime, user));
            }
            else
            	newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
        	}
        	else {
        		GeoPoint point = user.decidesNextPoint();
        		int arrivalTime = user.timeToReach(point);
        		newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user));
        	}
        } 
        else {
         // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
        	Station decision = user.determineStation();
        	
        	if (decision != null) { // user decides not to leave the system
        		int arrivalTime = user.timeToReach(decision.getPosition());
        		
        		if ( (user.decidesToReserveBike(decision)) && (ConfigInfo.reservationTime < arrivalTime) ) {
        			user.cancelsBikeReservation(decision);
        			newEvents.add(new EventBikeReservationTimeout(getInstant() + ConfigInfo.reservationTime, user));
        		}
        		newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant() + arrivalTime, user, decision));
        		
        	}
        }

        return newEvents;
    }
}