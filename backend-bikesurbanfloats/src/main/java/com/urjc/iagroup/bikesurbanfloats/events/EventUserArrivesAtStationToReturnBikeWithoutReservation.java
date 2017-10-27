package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

public class EventUserArrivesAtStationToReturnBikeWithoutReservation extends EventUser {
    private Station station;

    public EventUserArrivesAtStationToReturnBikeWithoutReservation(int instant, User user, Station station, SimulationConfiguration simulationConfiguration) {
        super(instant, user, simulationConfiguration);
        this.station = station;
    }
    
    public Station getStation() {
        return station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        user.returnBikeTo(station);
        return newEvents;
    }
    
    public String toString() {
        String str = super.toString();
        return str+"Station: "+station.toString();
    }
 
}
