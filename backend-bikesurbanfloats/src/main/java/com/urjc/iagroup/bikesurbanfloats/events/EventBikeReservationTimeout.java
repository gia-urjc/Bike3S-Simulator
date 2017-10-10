package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonSpecificBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventBikeReservationTimeout extends Event {
    private PersonSpecificBehaviour user;

    public EventBikeReservationTimeout(int instant, PersonSpecificBehaviour user) {
        super(instant);
        this.user = user;
    }

    public PersonSpecificBehaviour getUser() {
        return user;
    }

    public void setUser(PersonSpecificBehaviour user) {
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
    	return str+"User: "+user.toString()+"\n";
    }

}