package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToReturnBike extends Event {

    private Person user;
    private Station station;

    public EventUserArrivesAtStationToReturnBike(int instant, Person user, Station station) {
        super(instant);
        this.user = user;
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());

        boolean returnedBike = user.returnBikeTo(station);
        
        if(!returnedBike) {
        	newEvents.add(new EventUserDecidesReserveSlotOrReturnBike(getInstant(), user));
        }      
//        else if(!user.decidesToLeaveSystem()) {
//        	newEvents.add(new EventUserDecidesReserveOrRent(getInstant(), user));
//        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\nStation: "+station.toString();
    }

}