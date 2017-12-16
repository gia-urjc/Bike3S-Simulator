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
 * This class represents a tourist, so this user, after renting a bike, cycles to 
 * some place in the city in order to visit it.
 * Then, this user never decides to return directly the bike just after renting it.  
 * This type of user always chooses the longest route when he has rented a bike.
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_TOURIST)
public class UserTourist extends User {
	
	/**
	 * It indicates the size of the set of stations closest to the user within which the 
	 * destination will be chossen randomly.  
	 */
	private final int SELECTION_STATIONS_SET = 3;

	/**
	 * It is the maximum time in seconds until which the user will decide to continue walking 
	 * or cycling towards the previously chosen station witohout making a new reservation 
	 * after a reservation timeout event has happened.  
	 */
	private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;
	
	/**
	 * It is the place the tourist wants to visit after 
	 * renting 
	 * a b
	 * ike.
	 */
	private GeoPoint touristDestination;
	
	/**
	 * It contains the minum number of times that a fact must occur in order to decide to leave the system.  
	 */
	private MinParameters minParameters;
	
	/**
	 * It determines the rate with which the user will reserve a bike. 
	 */
	private int bikeReservationPercentage;
	
	/**
	 * It determines the rate with which the user will reserve a slot.
	 */
	private int slotReservationPercentage;
	
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

    public UserTourist() {
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
        		List<Station> recommendedStations = systemManager.getRecommendationSystem()
        		.recommendByLinearDistance(this.getPosition(), stations);
        		List<Station> nearestStations = new ArrayList<>();
        		
        		int end = SELECTION_STATIONS_SET;
        		if (SELECTION_STATIONS_SET > recommendedStations.size()) {
        			end = recommendedStations.size();
        		}

        		for(int i = 0; i < end; i++) {
        			nearestStations.add(recommendedStations.get(i));
        		}
         
        		int index = systemManager.getRandom().nextInt(0, SELECTION_STATIONS_SET - 1);
        		destination = nearestStations.get(index);
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
        		.recommendByLinearDistance(this.getPosition(), stations);
        List<Station> nearestStations = new ArrayList<>();
        
        for(int i = 0; i < SELECTION_STATIONS_SET; i++) {
        	nearestStations.add(recommendedStations.get(i));
        }
         
        int index = systemManager.getRandom().nextInt(0, SELECTION_STATIONS_SET - 1);
        return nearestStations.get(index);
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
     return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : systemManager.getRandom().nextBoolean();
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
    	return touristDestination;
    }

    @Override
    public boolean decidesToReturnBike() {
    	return false;
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
     * The user chooses the longest route because he wants to make a touristic travel as long as possible.
     */
    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        if(!hasBike()) {
        	return routes.get(0);	
        }
        else {
        	return routes.get(routes.size() - 1);
        }
    }

}