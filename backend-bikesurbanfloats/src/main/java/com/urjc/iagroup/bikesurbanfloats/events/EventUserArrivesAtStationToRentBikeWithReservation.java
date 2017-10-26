package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class EventUserArrivesAtStationToRentBikeWithReservation extends EventUser {
    private Station station;
    private Reservation reservation;

    public EventUserArrivesAtStationToRentBikeWithReservation(int instant, User user, Station station, Reservation reservation, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.station = station;
        this.reservation = reservation;
    }
    
    public Station getStation() {
        return station;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();;
        user.setPosition(station.getPosition());
        	reservation.resolve(instant);
        	user.addReservation(reservation);
        user.removeBikeFrom(station);
        if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
            newEvents = manageSlotReservationDecisionAtOtherStation();
        } else {   // user rides his bike to a point which is not a station
            GeoPoint point = user.decidesNextPoint();
            int arrivalTime = user.timeToReach(point);
            newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point, simulationConfiguration));
        }
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString()+"\n";
    }
    
}