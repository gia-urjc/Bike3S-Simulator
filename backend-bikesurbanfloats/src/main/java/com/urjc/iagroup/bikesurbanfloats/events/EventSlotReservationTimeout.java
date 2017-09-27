package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import java.util.List;
import java.util.ArrayList;

public class EventSlotReservationTimeout extends Event {
	private Person user;
	
	public EventSlotReservationTimeout(int instant, Person user) {
		super(instant);
		this.user = user;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}
	
	public List<Event> execute(){
		List<Event> newEvents = new ArrayList<Event>();
		return newEvents;

	}
	
	

}