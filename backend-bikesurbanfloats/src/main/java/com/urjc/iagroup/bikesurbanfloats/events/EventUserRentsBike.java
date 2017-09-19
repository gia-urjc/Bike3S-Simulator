package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;
import java.util.ArrayList;

public class EventUserRentsBike extends EventUser {
	private Bike bike;
	
	public EventUserRentsBike(int instant, User user, Station station, Bike bike) {
		super(instant, user, station, bike);
		this.bike = bike;
	}
	
	public Bike getBike() {
		return bike;
	}

	public void setBike(Bike bike) {
		this.bike = bike;
	}

	public List<Event> execute() {
		getUser().setBike(bike);
		station.subtractBike();
		
	}

}
