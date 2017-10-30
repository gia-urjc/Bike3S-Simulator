package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class EventUserArrivesAtStationToRentBikeWithReservation extends EventUser {
    private Station station;
    private Reservation reservation;

    public EventUserArrivesAtStationToRentBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
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
            user.decidesNextPoint();
            int arrivalTime = user.timeToReach();
            newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
        }
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString()+"\n";
    }
    
}