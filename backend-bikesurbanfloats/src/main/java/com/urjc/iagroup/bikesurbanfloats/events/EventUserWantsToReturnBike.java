package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventUserWantsToReturnBike extends Event {
    private Person user;

    public EventUserWantsToReturnBike(int instant, Person user) {
        super(instant);
        this.user = user;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        Station destination = user.determineStation();
        int arrivalTime = user.timeToReach(destination.getPosition());

        if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
            user.cancelsSlotReservation(destination);
            newEvents.add(new EventSlotReservationTimeout(getInstant() + arrivalTime, user));
        } else {
            newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }

}
