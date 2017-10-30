package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EventUserArrivesAtStationToReturnBikeWithReservation extends EventUser {
   
    private Station station;
    private Reservation reservation;


    public EventUserArrivesAtStationToReturnBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
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
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
       	reservation.resolve(instant);
       user.addReservation(reservation);

       if(!user.returnBikeTo(station)) {
        	newEvents = manageSlotReservationDecisionAtOtherStation();
        }      
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString();
    }

}