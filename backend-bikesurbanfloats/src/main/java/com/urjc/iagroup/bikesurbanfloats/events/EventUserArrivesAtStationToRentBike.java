package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToRentBike extends Event {

    private Person user;
    private Station station;

    public EventUserArrivesAtStationToRentBike(int instant, Person user, Station station) {
        super(instant);
        this.user = user;
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());

        boolean removedBike = user.removeBikeFrom(station);   // user tries to remove bike

        if (removedBike) {

            if (user.decidesToReturnBike()) {
                newEvents.add(new EventUserDecidesReserveSlotOrReturnBike(getInstant(), user));
            } else {
                GeoPoint point = user.decidesNextPoint();
                int arrivalTime = user.timeToReach(point);
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
            }
        } else {
            // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
            if (!user.decidesToLeaveSystem()) { 
                newEvents.add(new EventUserDecidesReserveOrRent(getInstant(), user));
            }
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\nStation: "+station.toString()+"\n";
    }
    
}