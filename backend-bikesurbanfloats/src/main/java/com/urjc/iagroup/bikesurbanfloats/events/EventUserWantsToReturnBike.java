package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition, SystemConfiguration systemInfo) {
        super(instant, user, systemInfo);
        this.actualPosition = actualPosition;
    }

    public GeoPoint getActualPosition() {
		return actualPosition;
	}

	public List<Event> execute() {
        user.setPosition(actualPosition);
        return manageSlotReservationDecision();
    }
    
}
