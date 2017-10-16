package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventBikeReservationTimeout extends Event {
    private Person user;

    public EventBikeReservationTimeout(int instant, Person user) {
        super(instant);
        this.user = user;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(SystemInfo.reservationTime);

        if (!user.decidesToLeaveSystem()) {
            Station destination = user.determineStation();
            user.setDestinationStation(destination);
            int arrivalTime = user.timeToReach(destination.getPosition());
            
            if (user.decidesToReserveBike(destination) && SystemInfo.reservationTime < arrivalTime) {
                user.cancelsBikeReservation(destination);
                newEvents.add(new EventBikeReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            } else {
                newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            }
        }
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	str += "| Destination" + user.getDestinationStation().getId();
    	return str+"User: "+user.toString()+"\n";
    }

}