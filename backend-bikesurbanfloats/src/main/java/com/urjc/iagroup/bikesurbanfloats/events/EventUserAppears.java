package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;

public class EventUserAppears extends EventUser {

    public EventUserAppears(int instant, User user, SystemConfiguration systemInfo) {
        super(instant, user, systemInfo);
    }
    
    public List<Event> execute() {
        return manageBikeReservationDecision();
    }
}