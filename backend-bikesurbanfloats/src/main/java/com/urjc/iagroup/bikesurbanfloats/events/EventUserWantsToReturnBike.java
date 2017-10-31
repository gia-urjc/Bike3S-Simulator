package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, User user, GeoPoint actualPosition) {
        super(instant, user);
        this.actualPosition = actualPosition;
    }

    public GeoPoint getActualPosition() {
		return actualPosition;
	}

	public List<Event> execute() throws Exception {
        user.setPosition(actualPosition);
        return manageSlotReservationDecisionAtOtherStation();
    }
    
}
