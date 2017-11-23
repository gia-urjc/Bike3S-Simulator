package com.urjc.iagroup.bikesurbanfloats.entities.users;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class Attempts {
	
	public enum AttemptCause {
		TIMEOUT, FAILED_RESERVATION, BIKES_UNAVAILABLE
	}
	
	private int counterReservationAttempts;
	private int counterAttemptsAfterReservationTimeout;
	private int counterAttemptsWhenBikesUnavailable;
	
	public Attempts() {
        this.counterReservationAttempts = 0; 
        this.counterAttemptsAfterReservationTimeout = 0;
        this.counterAttemptsWhenBikesUnavailable = 0;
	}
	
	public void update(AttemptCause cause) throws IllegalArgumentException {
		switch(cause) {
			case TIMEOUT: counterAttemptsAfterReservationTimeout++;
			case FAILED_RESERVATION: counterReservationAttempts++;
			case BIKES_UNAVAILABLE: counterAttemptsWhenBikesUnavailable++;
			default: throw new IllegalArgumentException(cause.toString() + "is not defined in update method");
		}
	}

}
