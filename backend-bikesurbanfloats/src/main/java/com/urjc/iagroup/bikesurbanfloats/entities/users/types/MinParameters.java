package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

/**
 * It provides attributes which determine the minimum number of times that a fact must 
 * happen before the user decides to leave the system.
 *  
 * @author IAgroup
 *
 */
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
