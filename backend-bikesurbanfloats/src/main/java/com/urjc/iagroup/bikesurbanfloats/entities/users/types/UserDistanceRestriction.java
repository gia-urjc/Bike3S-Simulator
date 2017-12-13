package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;
/**
 * This class represents a user whose behaviour is the same of UserReasonable with the 
 * exception that this user doesn't accept recommended stations which are farer that a 
 * certain distance. 
 * Then, if there are no stations to rent a bike which are nearer than the specified 
 * distance, he'll leave the system.
 * If there aren't any stations to return the bike nearer than the restrictive distance,
 * the user will go to the closest station.  
 * 
 * @author IAgroup
 *
 */
public class UserDistanceRestriction extends User {
	
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
	
	/**
	 * It is a distance restriction: this user dosn't go to destination stations which are 
	 * farer than this distance.  
	 */
	private double maxDistance;  

    public UserDistanceRestriction() {
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

     List<Station> recommendedStations = systemManager.getRecommendationSystem()
     		.recommendByProportionBetweenDistanceAndBikes(this.getPosition(), stations);
     
     // TODO: revise this code
     Station destination;
     try {
     destination = recommendedStations.stream().filter(station -> station
    		 .getPosition().distanceTo(this.getPosition()) <= maxDistance).findFirst().get();
     }
     catch (NullPointerException e) {
    	destination = null;
     }
     
     return destination;
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        
        if (stations.isEmpty()) {
        	stations = new ArrayList<>(systemManager.consultStations());
        }

        List<Station> recommendedStations = systemManager.getRecommendationSystem()
        		.recommendByProportionBetweenDistanceAndSlots(this.getPosition(), stations);
        
        Station destination;
        try {
        destination = recommendedStations.stream().filter(station -> station.getPosition()
        		.distanceTo(this.getPosition()) <= maxDistance).findFirst().get();
        }
        catch (NullPointerException e) {
        	destination = systemManager.getRecommendationSystem()
        			.recommendByLinearDistance(this.getPosition(), stations).get(0);
        }
        return destination;
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
