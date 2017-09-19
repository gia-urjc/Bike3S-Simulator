package com.urjc.iagroup.bikesurbanfloats.events;

public abstract class EventUser extends Event {
	private User user;
	private Station station;
	
	public EventUser(User user, Station station) {
		this.user=user;
		this.station=station;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}
}