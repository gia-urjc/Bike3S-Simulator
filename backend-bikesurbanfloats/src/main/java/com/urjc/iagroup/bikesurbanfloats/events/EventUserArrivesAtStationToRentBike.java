package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToRentBike extends EventUser {
    private Station station;

    public EventUserArrivesAtStationToRentBike(int instant, User user, Station station, SystemInfo systemInfo) {
        super(instant, user, systemInfo);
        this.station = station;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();;
        user.setPosition(station.getPosition());

        if (user.removeBikeFrom(station)) {
            if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
                newEvents = manageSlotReservationDecision();
            } else {   // user rides his bike to a point which is not a station
                GeoPoint point = user.decidesNextPoint();
                int arrivalTime = user.timeToReach(point);
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point, systemInfo));
            }
        } else {   // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
            if (!user.decidesToLeaveSystem(instant)) { 
                newEvents = manageBikeReservationDecision();
            }
        }
        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"Station: "+station.toString()+"\n";
    }
    
}