package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

import java.util.ArrayList;
import java.util.List;

public class EventUserAppears extends Event {
    private Person user;

    public EventUserAppears(int instant, Person user) {
        super(instant);
        this.user = user;
    }

    public Person getUser() {
        return user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        newEvents.add(new EventUserDecidesReserveOrRent(getInstant(), user));
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\n";
    }

}