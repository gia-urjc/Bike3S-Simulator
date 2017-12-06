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
 * This type of user always chooses the shortest routes. 
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_EMPLOYEE)
public class UserEmployee extends User {
	
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
	 * It is the street of the company where the user works.
	 */
	private GeoPoint companyStreet;

	/**
	 * It contains the attributes which characterizes the typical behaviour of this user type. 
	 */
	private UserTypeParameters parameters; 
	
    public UserEmployee() {
        super();
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == parameters.getMinReservationTimeouts() ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getCounterReservationAttempts() == parameters.getMinReservationAttempts() ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == parameters.getMinRentingAttempts() ? true : false;
    }
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        			List<Station> nearestStations = new ArrayList<>();
        
        for(int i = 0; i < SELECTION_STATIONS_SET; i++) {
            double minDistance = Double.MAX_VALUE;
            Station nearestStation = null;
        	for (Station currentStation: stations) {
            GeoPoint stationPosition = currentStation.getPosition();
            GeoPoint userPosition = getPosition();
            double distance = userPosition.distanceTo(stationPosition);
            if (!userPosition.equals(stationPosition) && distance < minDistance) {
                minDistance = distance;
                nearestStation = currentStation;
            }
        	}
        	nearestStations.add(nearestStation);
         stations.remove(nearestStation);
        }
        int index = systemManager.getRandom().nextInt(0, SELECTION_STATIONS_SET - 1);
        return nearestStations.get(index);
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        double minDistance = Double.MAX_VALUE;
        Station destination = null;
        for (Station currentStation: stations) {
            GeoPoint stationPosition = currentStation.getPosition();
            double distance = companyStreet.distanceTo(stationPosition);
            if (distance < minDistance) {
                minDistance = distance;
                destination = currentStation;
            }
        }
        return destination;
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
     return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : systemManager.getRandom().nextBoolean();
    }

    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getBikeReservationPercentage() ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getSlotReservationPercentage() ? true : false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
    	// TODO: how to throw the exception
    	return null;
    }

    @Override
    public boolean decidesToReturnBike() {
    	return true;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getReservationTimeoutPercentage() ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.getFailedReservationPercentage() ? true : false;
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