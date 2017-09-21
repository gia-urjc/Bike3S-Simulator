package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToReturnBike extends Event {

    private Person user;
    private Station station;

    public EventUserArrivesAtStationToReturnBike(int instant, Person user, Station station) {
        super(instant);
        this.user = user;
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        if (!user.returnBikeTo(station)) {
            Station destination = user.determineDestination();
            int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());
            newEvents.add(new EventUserArrivesAtStationToReturnBike(arrivalTime, user, destination));
        }

        return newEvents;
    }

}