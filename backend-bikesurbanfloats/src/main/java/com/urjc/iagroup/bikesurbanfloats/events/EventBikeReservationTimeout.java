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
    
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(Reservation.VALID_TIME);
        reservation.expire();
        user.addReservation(reservation);

        if (!user.decidesToLeaveSystemWhenTimeout(instant)) {
            if (!user.decidesToDetermineOtherStationWhenTimeout()){
                newEvents = manageBikeReservationDecisionAtSameStationAfterTimeout();
            }
            else {
                newEvents = manageBikeReservationDecisionAtOtherStation();
            }
        }
        return newEvents;
    }
}