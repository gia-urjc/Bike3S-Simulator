package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {

    public EventSlotReservationTimeout(int instant, Person user) {
        super(instant, user);
    }

    public List<Event> execute() {
        user.updatePosition(SystemInfo.reservationTime);
        return manageSlotReservationDecision();
    }
}