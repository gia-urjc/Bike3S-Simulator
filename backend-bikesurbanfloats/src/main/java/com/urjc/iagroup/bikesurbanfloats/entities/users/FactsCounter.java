package com.urjc.iagroup.bikesurbanfloats.entities.users;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class FactsCounter {
	
	public enum FactType {
		TIMEOUT, FAILED_RESERVATION, BIKES_UNAVAILABLE
	}
	
	private int counterReservationAttempts;
	private int counterReservationTimeoutEvents;
	private int counterAttemptsWhenBikesUnavailable;
	
	public FactsCounter() {
        this.counterReservationAttempts = 0; 
        this.counterReservationTimeoutEvents = 0;
        this.counterAttemptsWhenBikesUnavailable = 0;
	}
	
	public void update(FactType cause) throws IllegalArgumentException {
		switch(cause) {
			case TIMEOUT: counterReservationTimeoutEvents++;
			case FAILED_RESERVATION: counterReservationAttempts++;
			case BIKES_UNAVAILABLE: counterAttemptsWhenBikesUnavailable++;
			default: throw new IllegalArgumentException(cause.toString() + "is not defined in update method");
		}
	}

}
