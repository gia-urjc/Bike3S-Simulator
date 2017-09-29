package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

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

        Station destination = user.determineStation();
        int arrivalTime = user.timeToReach(destination.getPosition());

        if (user.decidesToReserveBike(destination) && SystemInfo.reservationTime < arrivalTime) {
            user.cancelsBikeReservation(destination);
            newEvents.add(new EventBikeReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
        } else {
            newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }

}