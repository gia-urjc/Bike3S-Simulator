package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.util.ReservationType;

public class Reservation {
	private int instant;
	private ReservationType type;
	private Person user; 
	private Station station;
	private Bike bike;  // bike which user wants to rent or return
	private boolean successful;  // reservation has been able to be made
	private boolean timeout;  // timeOut has ocurred before user arrival
	
	public Reservation(int instant, ReservationType type, Person user, Station station) {
		this.instant = instant;
		this.type = type;
		this.user = user;
		this.station = station;
		this.bike = null;
		this.successful = false;
		this.timeout = false;
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
	
public void setBike(Bike bike) {
		this.bike = bike;
	}

	public boolean getSuccessful() {
		return successful;
	}

	public boolean isTimeout() {
		return timeout;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	}