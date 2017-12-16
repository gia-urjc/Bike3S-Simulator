package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;

import java.util.List;
import java.util.ArrayList;

/**
 * This class represents a employee, i.e., a user who uses the bike as a public transport 
 * in order to arrive at work.
 * Then, this user always decides the destination station just after renting the bike 
 * in order to arrive at work as soon as possible.
 * Moreover, he always chooses both the closest origin station to himself and the closest destination 
 * station to his work. Also, he always chooses the shortest routes to get the stations.
 * Also, this type of user always determines a new destination station after 
 * a reservation failed attempt and always decides to continue to the previously chosen 
 * station after a timeout event with the intention of losing as little time as possible.
 * And, of course, he never leaves the system as he needs to ride on bike in order to arrive at work. 
 *   
 * @author IAgroup
  */
@AssociatedType(UserType.USER_EMPLOYEE)
public class UserEmployee extends User {
	
	/**
	 * It is the time in seconds until which the user will decide to continue walking 
	 * or cycling towards the previously chosen station without making a new reservation 
	 * after a reservation timeout event has happened.  
	 */
	private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;
	
	/**
	 * It is the street of the company where the user works.
	 */
	private GeoPoint companyStreet;
	
	/**
	 * It determines the rate with which the user will reserve a bike. 
	 */
	private int bikeReservationPercentage;
	
	/**
	 * It determines the rate with which the user will reserve a slot.
	 */
	private int slotReservationPercentage;
	
	/**
	 * It contains the minum number of times that a fact must occur in order to decide to leave the system.
	 */
	private MinParameters minParameters;
	
    public UserEmployee() {
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
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        Station destination = null;
        
        if (!stations.isEmpty()) {
        	destination = systemManager.getRecommendationSystem().recommendByLinearDistance(this
        			.getPosition(), stations).get(0);
        }
        
        return destination; 
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);

        if (stations.isEmpty()) {
        	stations = systemManager.consultStations();  
        }
        
        return systemManager.getRecommendationSystem().recommendByLinearDistance(this
    			.getPosition(), stations).get(0); 
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
     return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < bikeReservationPercentage ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < slotReservationPercentage ? true : false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
    	// TODO: check it
    	System.out.println("This user mustn't cycle to a place which isn't a station");
    	return null;
    }

    @Override
    public boolean decidesToReturnBike() {
    	return true;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
    		return true;
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