package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EventUserArrivesAtStationToReturnBike extends EventUser {
   
    private Station station;
    private Reservation reservation;


    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, Reservation reservation, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.station = station;
        this.reservation = reservation;
    }
    
    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.station = station;
        this.reservation = null;
    }


    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        if(reservation != null) {
        	reservation.resolve(instant);
            user.addReservation(reservation);
        }
        
        if(!user.returnBikeTo(station)) {
        	newEvents = manageSlotReservationDecision();
        }      
       
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString();
    }

}