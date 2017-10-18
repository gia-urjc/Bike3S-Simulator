package com.urjc.iagroup.bikesurbanfloats.entities;


public class Reservation {
	private int instant;
	private ReservationType type;
	private Person user; 
	private Station station;
	private Bike bike;  // bike which user wants to rent or return
	
	public Reservation(int instant, ReservationType type, Person user, Station station, Bike bike) {
		this.instant = instant;
		this.type = type;
		this.user = user;
		this.station = station;
		this.bike = bike;
	}

	public int getInstant() {
		return instant;
	}
	
	public ReservationType getType() {
		return type;
	}
	
	public Person getUser() {
		return user;
	}
	
	public Station getStation() {
		return station;
	}
	
	public Bike getBike() {
		return bike;
	}

	
}
