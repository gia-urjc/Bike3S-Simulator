package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;

import java.util.ArrayList;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
    
    public EventBikeReservationTimeout(int instant, Person user) {
        super(instant, user);
    }
    
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(SystemInfo.reservationTime);

        if (!user.decidesToLeaveSystem(instant)) {
        	newEvents = manageBikeReservationDecision();
        }
        return newEvents;
    }
}