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
        	newEvents.add(new EventUserDecidesReserveOrRent(getInstant(), user));
        }
        
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	str += "| Destination" + user.getDestinationStation().getId();
    	return str+"User: "+user.toString()+"\n";
    }

}