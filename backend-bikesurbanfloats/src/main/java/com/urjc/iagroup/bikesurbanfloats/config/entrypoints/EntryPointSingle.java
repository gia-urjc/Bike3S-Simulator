package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserType;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.util.*;

public class EntryPointSingle implements EntryPoint {
	private GeoPoint position;
	private UserType personType;
	private int instant; 
	
	public EntryPointSingle(GeoPoint position, UserType personType, int instant) {
		this.position = position;
		this.personType = personType;
		this.instant = instant;
	}

	@Override
	public List<EventUserAppears> generateEvents(SystemInfo systemInfo) {
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory personFactory = new UserFactory();
		IdGenerator userIdGenerator = systemInfo.userIdGenerator;
		int id = userIdGenerator.next();
		User person = personFactory.createPerson(id, personType, position, systemInfo);
		EventUserAppears event = new EventUserAppears(instant, person, systemInfo);
		generatedEvents.add(event);
		return generatedEvents;
	}
	
	public String toString() {
		String result = position.toString();
		result += "| SINGLE PERSON \n";
		result += "Person Type: " + personType + "\n";
		result += "Instant: " + instant + "\n";
		return result;
	}
}
