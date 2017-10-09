package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class EventUserArrivesAtStationToRentBike extends Event {

    private PersonBehaviour user;
    private Station station;

    public EventUserArrivesAtStationToRentBike(int instant, PersonBehaviour user, Station station) {
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
                Station destination = user.determineStation();
                user.setDestinationStation(destination);
                int arrivalTime = getInstant() + user.timeToReach(destination.getPosition());

                if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
                    user.cancelsSlotReservation(destination);
                    newEvents.add(new EventSlotReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
                } else {
                    newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
                }
            } else {
                GeoPoint point = user.decidesNextPoint();
                int arrivalTime = user.timeToReach(point);
                newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
            }
        } else {
            // there're not bikes: user decides to go to another station, to reserve a bike or to leave the simulation
            
            if (!user.decidesToLeaveSystem()) { 
                Station decision = user.determineStation();
                user.setDestinationStation(decision);
                int arrivalTime = user.timeToReach(decision.getPosition());
                
                if (user.decidesToReserveBike(decision) && SystemInfo.reservationTime < arrivalTime) {
                    user.cancelsBikeReservation(decision);
                    newEvents.add(new EventBikeReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
                } else {
                    newEvents.add(new EventUserArrivesAtStationToRentBike(getInstant() + arrivalTime, user, decision));
                }
            }
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\nStation: "+station.toString()+"\n";
    }
    
}