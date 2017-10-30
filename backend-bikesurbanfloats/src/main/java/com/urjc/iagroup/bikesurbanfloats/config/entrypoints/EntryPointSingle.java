package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class EntryPointSingle extends EntryPoint {
	private GeoPoint position;
	private UserType userType;
	private int instant; 
	
	public EntryPointSingle(GeoPoint position, UserType userType, int instant) {
		this.position = position;
		this.userType = userType;
		this.instant = instant;
	}

	@Override
	public List<EventUserAppears> generateEvents() {
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		User user = userFactory.createUser(userType);
		EventUserAppears event = new EventUserAppears(instant, user, position);
		generatedEvents.add(event);
		return generatedEvents;
	}
	
	public String toString() {
		String result = position.toString();
		result += "| SINGLE user \n";
		result += "user Type: " + userType + "\n";
		result += "Instant: " + instant + "\n";
		return result;
	}
}
