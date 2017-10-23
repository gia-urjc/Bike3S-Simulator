package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.*;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {
	private Reservation reservation;


    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, SystemConfiguration systemConfig) {
        super(instant, user, systemConfig);
        this.reservation = reservation;
    }
    
    public EventSlotReservationTimeout(int instant, User user, SystemConfiguration systemConfig) {
        super(instant, user, systemConfig);
        this.reservation = null;
    }

    public List<Event> execute() {

        user.updatePosition(systemConfig.getReservationTime());
        reservation.expire();
        user.addReservation(reservation);
        return manageSlotReservationDecision();
    }
}