package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public class EventUserArrivesAtStationToRentBikeWithoutReservation extends EventUser {
    private Station station;
    
    public EventUserArrivesAtStationToRentBikeWithoutReservation(int instant, User user, Station station) {
        super(instant, user);
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();;
        user.setPosition(station.getPosition());
        
        if (user.removeBikeWithoutReservationFrom(station)) {
            if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
                newEvents = manageSlotReservationDecisionAtOtherStation();
            } else {   // user rides his bike to a point which is not a station
                GeoPoint point = user.decidesNextPoint();
                int arrivalTime = user.timeToReach(point);
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
            }
        } else {   // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
            if (!user.decidesToLeaveSystemWhenBikesUnavailable(instant)) { 
                newEvents = manageBikeReservationDecisionAtOtherStation();
            }
        }
        return newEvents;
    }
    
    public String toString() {
        String str = super.toString();
        return str+"Station: "+station.toString()+"\n";
    }
}
