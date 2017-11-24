package com.urjc.iagroup.bikesurbanfloats.entities.users;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class UserMemory {
	
	public enum FactType {
		BIKE_RESERVATION_TIMEOUT, BIKE_FAILED_RESERVATION, BIKES_UNAVAILABLE
	}
	
	private int counterReservationAttempts;
	private int counterReservationTimeouts;
	private int counterRentingAttemptsWhenBikesUnavailable;
	
	public UserMemory() {
        this.counterReservationAttempts = 0; 
        this.counterReservationTimeouts = 0;
        this.counterRentingAttemptsWhenBikesUnavailable = 0;
	}
	
	public int getCounterRentingAttemptsWhenBikesUnavailable() {
		return counterRentingAttemptsWhenBikesUnavailable;
	}

	public int getCounterReservationAttempts() {
		return counterReservationAttempts;
	}

	public int getCounterReservationTimeouts() {
		return counterReservationTimeouts;
	}

	public void update(FactType fact) throws IllegalArgumentException {
		switch(fact) {
			case BIKE_RESERVATION_TIMEOUT: counterReservationTimeouts++;
			case BIKE_FAILED_RESERVATION: counterReservationAttempts++;
			case BIKES_UNAVAILABLE: counterAttemptsWhenBikesUnavailable++;
			default: throw new IllegalArgumentException(cause.toString() + "is not defined in update method");
		}
	}

}
