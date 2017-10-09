package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.PersonBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

import java.util.ArrayList;
import java.util.List;

public class EventUserAppears extends Event {
    private PersonBehaviour user;

    public EventUserAppears(int instant, PersonBehaviour user) {
        super(instant);
        this.user = user;
    }

    public PersonBehaviour getUser() {
        return user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        Station destination = user.determineStation();
        user.setDestinationStation(destination);
        int arrivalTime = user.timeToReach(destination.getPosition());

        if (user.decidesToReserveBike(destination) && SystemInfo.reservationTime < arrivalTime) {
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