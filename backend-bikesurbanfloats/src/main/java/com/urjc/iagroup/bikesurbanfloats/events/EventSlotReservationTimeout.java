package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {

    public EventSlotReservationTimeout(int instant, User user, SystemConfiguration systemInfo) {
        super(instant, user, systemInfo);
    }

    public List<Event> execute() {
        user.updatePosition(systemInfo.getReservationTime());
        return manageSlotReservationDecision();
    }
}