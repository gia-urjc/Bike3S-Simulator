package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import java.util.List;
import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

/**
 * This class represents a user who always follows the frist system recommendation with 
 * respect a proportion, i. e., he weighs what is the best recommendation (the most little 
 * value) in relation to the quotient between the distance from his position to the station 
 * and the number of available bikes or slots which it contains.   
 * This user always reserves bikes and slots at destination stations to ensure his service 
 * as he knows the stations he chooses as destination may not have too many bikes or slots. 
 * Moreover, he always chooses the shortest routes to get his destination.
 * 
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_WEIGHER)
public class UserReasonable extends User {
	
	/**
	 * It indicates the size of the set of stations closest to the user within which the 
	 * destination will be chossen randomly.  
	 */
	private final int SELECTION_STATIONS_SET = 3;
	
	/**
	 * It is the time in seconds until which the user will decide to continue walking 
	 * or cycling towards the previously chosen station without making a new reservation 
	 * after a reservation timeout event has happened.  
	 */
	private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;
	
	/**
	 * It contains the minum number of times that a fact must occur in order to decide to leave the system.  
	 */
	private MinParameters minParameters;
	
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

	
    public UserReasonable() {
        super();
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == minParameters.getMinReservationTimeouts() ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
    	return getMemory().getCounterReservationAttempts() == minParameters.getMinReservationAttempts() ? true : false; 
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == minParameters.getMinRentingAttempts() ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike(int instant) {
    	List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
    	
     if (stations.isEmpty()) {
     	stations = new ArrayList<>(systemManager.consultStations());
     }

    	return systemManager.getRecommendationSystem()
    			.recommendByProportionBetweenDistanceAndBikes(this.getPosition(), stations).get(0);
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        
        if (stations.isEmpty()) {
        	stations = new ArrayList<>(systemManager.consultStations());
        }

        return systemManager.getRecommendationSystem()
        		.recommendByProportionBetweenDistanceAndSlots(this.getPosition(), stations).get(0);
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	return true;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	return true;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < bikeReturnPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < failedReservationPercentage ? true : false;
    }
    
    /**
     * The user chooses the shortest route because he wants to arrive at work as fast as possible.
     */
    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        // The route in first list position is the shortest.
        return routes.get(0);
    }
	

}
