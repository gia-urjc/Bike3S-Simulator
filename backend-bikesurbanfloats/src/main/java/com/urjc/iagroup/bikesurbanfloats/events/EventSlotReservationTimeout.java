package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;

public class EventSlotReservationTimeout extends EventUser {
	private Reservation reservation;


    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.reservation = reservation;
    }
    
    public EventSlotReservationTimeout(int instant, User user, SimulationConfiguration simulationConfiguration) {
        this(instant, user, null, simulationConfiguration);
    }

    public List<Event> execute() {

        user.updatePosition(simulationConfiguration.getReservationTime());
        reservation.expire();
        user.addReservation(reservation);
        return manageSlotReservationDecision();
    }
}