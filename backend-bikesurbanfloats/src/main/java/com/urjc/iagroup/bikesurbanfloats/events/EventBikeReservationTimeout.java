package com.urjc.iagroup.bikesurbanfloats.events;


import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
	private Reservation reservation;
    

    public EventBikeReservationTimeout(int instant, User user, Reservation reservation, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.reservation = reservation;
    }
    
    
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(simulationConfiguration.getReservationTime());
        reservation.expire();
        user.addReservation(reservation);

        if (!user.decidesToLeaveSystem(instant)) {
        	newEvents = manageBikeReservationDecision();
        }
        return newEvents;
    }
}