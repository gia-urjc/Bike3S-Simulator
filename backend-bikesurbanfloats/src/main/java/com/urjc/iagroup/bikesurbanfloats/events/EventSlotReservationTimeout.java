package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.List;
import java.util.ArrayList;

public class EventSlotReservationTimeout extends EventUser {
	private Reservation reservation;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.updatePosition(simulationConfiguration.getReservationTime());
        reservation.expire();
        user.addReservation(reservation);
        if (!user.decidesToDetermineOtherStationWhenTimeout()){
            newEvents = manageSlotReservationDecisionAtSameStationAfterTimeout();
        }
        else
            newEvents = manageSlotReservationDecisionAtOtherStation();
        return newEvents;
    }
}