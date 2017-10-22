package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
    
    public EventBikeReservationTimeout(int instant, User user, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
    }
    
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(systemInfo.reservationTime);

        if (!user.decidesToLeaveSystem(instant)) {
        	newEvents = manageBikeReservationDecision();
        }
        return newEvents;
    }
}