package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

import java.util.List;

public class EventUserAppears extends EventUser {

    private GeoPoint position;
	
	public EventUserAppears(int instant, User user, GeoPoint position) {
        super(instant, user);
        this.position = position;
    }
    
    public List<Event> execute() {
    	user.setPosition(position);
        return manageBikeReservationDecisionAtOtherStation();
    }
}