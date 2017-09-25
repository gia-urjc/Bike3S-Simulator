package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.ArrayList;
import java.util.List;

public class EventUserAppears extends Event {

    private Person user;

    public EventUserAppears(int instant, Person user) {
        super(instant);
        this.user = user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        Station destination = user.determineDestination();
        int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());

        newEvents.add(new EventUserArrivesAtStationToRentBike(arrivalTime, user, destination));

        return newEvents;
    }

}