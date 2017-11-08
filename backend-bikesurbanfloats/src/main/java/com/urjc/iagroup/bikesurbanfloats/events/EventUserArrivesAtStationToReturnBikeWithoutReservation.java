package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;

public class EventUserArrivesAtStationToReturnBikeWithoutReservation extends EventUser {

    private List<Entity> entities;
    private Station station;

    public EventUserArrivesAtStationToReturnBikeWithoutReservation(int instant, User user, Station station) {
        super(instant, user);
        this.entities = Arrays.asList(user, station);
        this.station = station;
    }
    
    public Station getStation() {
        return station;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        if(!user.returnBikeWithoutReservationTo(station)) {
        	newEvents = manageSlotReservationDecisionAtOtherStation();
        }      
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
