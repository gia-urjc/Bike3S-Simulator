package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToReturnBike extends Event {

    private PersonBehaviour user;
    private Station station;

    public EventUserArrivesAtStationToReturnBike(int instant, PersonBehaviour user, Station station) {
        super(instant);
        this.user = user;
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());

        if (!user.returnBikeTo(station)) {
            Station destination = user.determineStation();
            user.setDestinationStation(destination);
            int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());

            if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
                user.cancelsSlotReservation(destination);
                newEvents.add(new EventSlotReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
            } else {
                newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
            }
        }
        
        if(!user.decidesToLeaveSystem()) {
        	if(!user.decidesToRentBikeAtOtherStation()) {
        		newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant(), user, station));
        	}
        	else {
        		Station destination = user.determineStation();
        		user.setDestinationStation(destination);
        		int arrivalTime = user.timeToReach(destination.getPosition());
        		newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant() + arrivalTime, user, destination));
        	}
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.getId()+"\nStation: "+station.getId();
    }

}