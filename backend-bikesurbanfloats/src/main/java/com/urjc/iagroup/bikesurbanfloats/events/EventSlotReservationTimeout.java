package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {
	private Reservation reservation;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
        this.reservation = reservation;
    }
    
    public EventSlotReservationTimeout(int instant, User user, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
        this.reservation = null;
    }

    public List<Event> execute() {

        user.updatePosition(systemInfo.getReservationTime());
        reservation.expire();
        user.addReservation(reservation);
        return manageSlotReservationDecision();
    }
}