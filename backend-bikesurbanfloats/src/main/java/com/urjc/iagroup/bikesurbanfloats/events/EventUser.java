package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.*; 

public abstract class EventUser extends Event {
	private Person user;
	private Station station;
	
	public EventUser(int instant, Person user, Station station) {
		super(instant);
		this.user = user;
		this.station = station;
	}

	public  Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}
}