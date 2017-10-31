package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;
import java.util.ArrayList;

public class EventSlotReservationTimeout extends EventUser {
	private Reservation reservation;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation) {
        super(instant, user);
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.updatePositionAfterTimeOut();
        reservation.expire();
        user.addReservation(reservation);
        user.cancelsSlotReservation(user.getDestinationStation());
        
        if (!user.decidesToDetermineOtherStationAfterTimeout()){
            newEvents = manageSlotReservationDecisionAtSameStationAfterTimeout();
        }
        else
            newEvents = manageSlotReservationDecisionAtOtherStation();
        return newEvents;
    }
}