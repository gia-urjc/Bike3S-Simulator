package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;

public class EventUserAppears extends EventUser {

    public EventUserAppears(int instant, User user, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
    }
    
    public List<Event> execute() {
        return manageBikeReservationDecision();
    }
}