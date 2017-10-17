package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;

import java.util.List;

public class EventUserAppears extends EventUser {

    public EventUserAppears(int instant, Person user) {
        super(instant, user);
    }
    
    public List<Event> execute() {
        return manageBikeReservationDecision();
    }
}