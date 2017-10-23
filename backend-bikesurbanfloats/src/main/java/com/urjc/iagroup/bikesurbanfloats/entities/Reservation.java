package com.urjc.iagroup.bikesurbanfloats.entities;

public class Reservation {
	
	public enum ReservationType {
		SLOT, BIKE
	}
	
	public enum ReservationState {
		FAILED, ACTIVE, EXPIRED, SUCCESSFUL
	}
	
	private static int timeout;	
	private int startInstant;  // instant when user makes the reservation
	private int endInstant;  // instant when reservation is resolved or expired
	private ReservationType type;
	private ReservationState state;
	private User user; 
	private Station station;
	private Bike bike;  // bike which user has reserved or wants to return
	
	public Reservation(int startInstant, ReservationType type, User user, Station station, Bike bike) {
		this.startInstant = startInstant;
		this.endInstant = -1; // reservation has'nt ended
		this.type = type;
		this.state = ReservationState.ACTIVE;
		this.user = user;
		this.station = station;
		this.bike = bike;
			}
	
	public Reservation(int startInstant, ReservationType type, User user, Station station) {
		this.startInstant = startInstant;
		this.endInstant = -1; // reservation has'nt ended
		this.type = type;
		this.state = ReservationState.FAILED;
		this.user = user;
		this.station = station;
		this.bike = null;
			}
	
	public void init(int time) {
		timeout = time;
	}

	public int getStartInstant() {
		return startInstant;
	}
	
	public ReservationType getType() {
		return type;
	}
	
	public ReservationState getState() {
		return state;
	}
	
	public User getUser() {
		return user;
	}
	
	public Station getStation() {
		return station;
	}
	
	public Bike getBike() {
		return bike;
	}

public void expire() {
	this.state = ReservationState.EXPIRED;
	this.endInstant = this.startInstant + timeout;
}

public void resolve(int endInstant) {
	this.state = ReservationState.SUCCESSFUL;
	this.endInstant = endInstant;
}
	}