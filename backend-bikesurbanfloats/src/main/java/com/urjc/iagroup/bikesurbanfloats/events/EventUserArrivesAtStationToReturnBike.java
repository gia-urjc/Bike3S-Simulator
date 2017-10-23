package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.ArrayList;
import java.util.List;

public class EventUserArrivesAtStationToReturnBike extends EventUser {
   
    private Station station;

    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, SystemConfiguration systemInfo) {
        super(instant, user, systemInfo);
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());

        if(!user.returnBikeTo(station)) {
        	newEvents = manageSlotReservationDecision();
        }      
       
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString();
    }

}