package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.*;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.util.*;

public class EntryPointSingle implements EntryPoint {
	private GeoPoint position;
	private UserType userType;
	private int instant; 
	
	public EntryPointSingle(GeoPoint position, UserType userType, int instant) {
		this.position = position;
		this.userType = userType;
		this.instant = instant;
	}

	@Override
	public List<EventUserAppears> generateEvents(SystemConfiguration systemConfig) {
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		IdGenerator userIdGenerator = systemConfig.getUserIdGenerator();
		int id = userIdGenerator.next();
		User user = userFactory.createUser(id, userType, position, systemConfig);
		EventUserAppears event = new EventUserAppears(instant, user, systemConfig);
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
