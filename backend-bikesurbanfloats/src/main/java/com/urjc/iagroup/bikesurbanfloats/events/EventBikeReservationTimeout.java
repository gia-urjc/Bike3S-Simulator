package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
	private Reservation reservation;
    
    public EventBikeReservationTimeout(int instant, User user, Reservation reservation) {
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
        user.cancelsBikeReservation(user.getDestinationStation());

        if (!user.decidesToLeaveSystemAfterTimeout(instant)) {
            if (!user.decidesToDetermineOtherStationAfterTimeout()){
                newEvents = manageBikeReservationDecisionAtSameStationAfterTimeout();
            }
            else {
                newEvents = manageBikeReservationDecisionAtOtherStation();
            }
        }
        return newEvents;
    }
}