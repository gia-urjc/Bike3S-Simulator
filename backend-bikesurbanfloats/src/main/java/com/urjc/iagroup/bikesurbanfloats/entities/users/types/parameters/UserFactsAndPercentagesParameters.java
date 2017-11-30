package com.urjc.iagroup.bikesurbanfloats.entities.users.types.parameters;

public class UserFactsAndPercentagesParameters {
	
	/**
	 * It is the number of times that the user musts try to make a bike reservation before 
	 * deciding to leave the system.  
	 */
	private static int minReservationAttempts;
	
	/**
	 * It is the number of times that a reservation timeout event musts occurs before the 
	 * user decides to leave the system.
	 */
	private static int minReservationTimeouts;
	
 /**
  * It is the number of times that the user musts try to rent a bike (without a bike 
  * reservation) before deciding to leave the system.	
  */
	private static int minRentingAttempts;
	
	/**
	 * It determines the rate with which the user will reserve a bike. 
	 */
	private static int bikeReservationPercentage;
	
	/**
	 * It determines the rate with which the user will reserve a slot.
	 */
	private static int slotReservationPercentage;
	
	/**
	 * It determines the rate with which the user will decide to go directly to a station 
	 * in order to return the bike he has just rented.  
	 */
	private static int bikeReturnPercentage;
	
	/**
	 * It determines the rate with which the user will choose a new destination station 
	 * after a  timeout event happens.
	 */
	private static int reservationTimeoutPercentage;
	
	/**
	 * It determines the rate with which the user will choose a new destination station
	 * after he hasn't been able to make a reservation. 
	 */
	private static int failedReservationPercentage;
	
	/**
	 * It is the minimum distance that the user will travel since he rents a bike
	 * until he returns it.
	 */
	private static int minDistanceToTravel;
	


}
