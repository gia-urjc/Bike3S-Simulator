package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.PersonSpecificBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

import java.util.ArrayList;
import java.util.List;

public class EventUserAppears extends Event {
    private PersonSpecificBehaviour user;

    public EventUserAppears(int instant, PersonSpecificBehaviour user) {
        super(instant);
        this.user = user;
    }

    public PersonSpecificBehaviour getUser() {
        return user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        Station destination = user.determineStation();
        user.setDestinationStation(destination);
        int arrivalTime = user.timeToReach(destination.getPosition());

        if (user.getDestinationStation().availableBikes() > 0 && user.decidesToReserveBike(destination) && SystemInfo.reservationTime < arrivalTime) {
            user.cancelsBikeReservation(destination);
            newEvents.add(new EventBikeReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
        } else {
			newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\n";
    }

}