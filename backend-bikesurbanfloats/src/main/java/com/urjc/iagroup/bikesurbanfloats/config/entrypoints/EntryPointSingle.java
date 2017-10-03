package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.LinkedList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonFactory;
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

	public GeoPoint getPosition() {
		return position;
	}

	public void setPosition(GeoPoint position) {
		this.position = position;
	}

	public PersonType getPersonType() {
		return personType;
	}

	public void setPersonType(PersonType personType) {
		this.personType = personType;
	}
	
	
	public int getInstant() {
		return instant;
	}

	public void setInstant(int instant) {
		this.instant = instant;
	}

	@Override
	public List<Event> generateEvents(IdGenerator personIdGenerator) {
		List<Event> generatedEvents = new LinkedList<>();
		PersonFactory personFactory = new PersonFactory();
		int id = personIdGenerator.next();
		Person person = personFactory.createPerson(id, personType, position);
		Event event = new EventUserAppears(instant, person);
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
