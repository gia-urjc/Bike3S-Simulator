package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {

    public EventSlotReservationTimeout(int instant, Person user, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
    }

    public List<Event> execute() {
        user.updatePosition(systemInfo.getReservationTime());
        return manageSlotReservationDecision();
    }
}