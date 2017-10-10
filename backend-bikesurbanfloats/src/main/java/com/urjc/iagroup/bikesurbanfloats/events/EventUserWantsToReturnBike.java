package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.PersonSpecificBehaviour;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class EventUserWantsToReturnBike extends Event {
    private PersonSpecificBehaviour user;
    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, PersonSpecificBehaviour user, GeoPoint actualPosition) {
        super(instant);
        this.user = user;
        this.actualPosition = actualPosition;
    }

    public PersonSpecificBehaviour getUser() {
        return user;
    }

    public void setUser(PersonSpecificBehaviour user) {
        this.user = user;
    }

    public GeoPoint getActualPosition() {
		return actualPosition;
	}

	public void setActualPosition(GeoPoint actualPosition) {
		this.actualPosition = actualPosition;
	}

	public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(actualPosition);

        Station destination = user.determineStation();
        user.setDestinationStation(destination);
        int arrivalTime = user.timeToReach(destination.getPosition());

        if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
            user.cancelsSlotReservation(destination);
            newEvents.add(new EventSlotReservationTimeout(getInstant() + arrivalTime, user));
        } else {
            newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.toString()+"\n";
    }

}
