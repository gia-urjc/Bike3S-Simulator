package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

public class MinParameters {
	
	/**
	 * It is the number of times that the user musts try to make a bike reservation before 
	 * deciding to leave the system.  
	 */
	private int minReservationAttempts;
	
	/**
	 * It is the number of times that a reservation timeout event musts occurs before the 
	 * user decides to leave the system.
	 */
	private int minReservationTimeouts;
	
 /**
  * It is the number of times that the user musts try to rent a bike (without a bike 
  * reservation) before deciding to leave the system.	
  */
	private int minRentingAttempts;
	
	/**
	 * It determines the rate with which the user will reserve a bike. 
	 */
	private int bikeReservationPercentage;
	
	/**
	 * It determines the rate with which the user will reserve a slot.
	 */
	private int slotReservationPercentage;
	
	/**
	 * It determines the rate with which the user will decide to go directly to a station 
	 * in order to return the bike he has just rented.  
	 */
	private int bikeReturnPercentage;
	
	/**
	 * It determines the rate with which the user will choose a new destination station 
	 * after a  timeout event happens.
	 */
	private int reservationTimeoutPercentage;
	
	/**
	 * It determines the rate with which the user will choose a new destination station
	 * after he hasn't been able to make a reservation. 
	 */
	private int failedReservationPercentage;

	public int getMinReservationAttempts() {
		return minReservationAttempts;
	}

	public int getMinReservationTimeouts() {
		return minReservationTimeouts;
	}

	public int getMinRentingAttempts() {
		return minRentingAttempts;
	}

}
