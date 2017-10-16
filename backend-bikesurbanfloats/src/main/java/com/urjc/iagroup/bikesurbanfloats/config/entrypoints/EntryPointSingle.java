package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonType;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.util.*;

public class EntryPointSingle implements EntryPoint {
	private GeoPoint position;
	private PersonType personType;
	private int instant; 
	
	public EntryPointSingle(GeoPoint position, PersonType personType) {
		this.position = position;
		this.personType = personType;
	}

	@Override
	public List<EventUserAppears> generateEvents(IdGenerator personIdGenerator) {
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		PersonFactory personFactory = new PersonFactory();
		int id = personIdGenerator.next();
		Person person = personFactory.createPerson(id, personType, position);
		EventUserAppears event = new EventUserAppears(instant, person);
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
