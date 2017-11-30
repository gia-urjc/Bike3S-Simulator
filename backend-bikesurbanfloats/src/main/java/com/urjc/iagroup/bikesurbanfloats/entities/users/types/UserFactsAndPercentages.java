package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_FACTS)
public class UserFactsAndPercentages extends User {
	/**
	 * It indicates the size of the set of stations closest to the user within which the 
	 * destination will be chossen randomly.  
	 */
	private final int SELECTION_STATIONS_SET = 3;
	
	/**
	 * It is the maximum time in seconds until which the user will decide to continue walking 
	 * or cycling towards the previously chosen station after a reservation timeout event.  
	 */
	private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;
	
	/**
	 * 
	 */
	private int traveledDistance;
	
	private UserFactsAndPercentagesParameters parameters; 
	
    public UserFactsAndPercentages() {
        super();
        this.traveledDistance = 0;
    }
    
    public static void setMinimums(int reservationAttempts, int reservationTimeouts, int rentingAttempts) {
    	minReservationAttempts = reservationAttempts;
    	minReservationTimeouts = reservationTimeouts;
    	minRentingAttempts = rentingAttempts;
    }
    
    public static void setPercentages(int bikeReservation, int slotReservation, int reservationTimeout, int failedReservation) {
    	bikeReservationPercentage = bikeReservation;
    	slotReservationPercentage = slotReservation;
    	reservationTimeoutPercentage = reservationTimeout;
    	failedReservationPercentage = failedReservation;
    }
    
    public static void setMinDistance(int distance) {
    	minDistanceToTravel = distance;
    }
    
    @Override
    public void setRoute(GeoRoute route) {
    	super.setRoute(route);
    	traveledDistance += route.getTotalDistance(); 
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getCounterReservationAttempts() == minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == minRentingAttempts ? true : false;
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
        List<Station> stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);
        Station destination = null;
        boolean found = false;
        int index = 0;
        GraphManager graph = systemManager.getGraphManager();
        List<GeoRoute> routes = null;
        
        while (!stations.isEmpty()) {
        	index = systemManager.getRandom().nextInt(0, stations.size());
        	destination = stations.get(index);
        	try {
        		graph.calculateRoutes(this.getPosition(), destination.getPosition());
        		routes = graph.getAllRoutes();
        	}
        	catch(Exception e) {
        		System.out.println("Routes not found: "+e.getMessage());
        		continue;
        	}
        	
        	for(GeoRoute route: routes) {
        		if(route.getTotalDistance() >= minDistanceToTravel - traveledDistance)
        			return destination;
        	}
        	stations.remove(destination);
        	destination = null;
        }
        
        if (destination == null) {
        	stations = systemManager.consultStations();
        	index = systemManager.getRandom().nextInt(0, stations.size());
        	destination = stations.get(index); 
        }
        
        return destination;
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
     return arrivalTime <= MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : systemManager.getRandom().nextBoolean();
    }

    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 101);
    	return percentage <= bikeReservationPercentage ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime <= MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	int percentage = systemManager.getRandom().nextInt(0, 101);
    	return percentage <= slotReservationPercentage ? true : false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
    	int percentage = systemManager.getRandom().nextInt(0, 101);
    	return percentage <= bikeReturnPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	int percentage = systemManager.getRandom().nextInt(0, 101);
    	return percentage <= reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = systemManager.getRandom().nextInt(0, 101);
        return percentage <= failedReservationPercentage ? true : false;
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        // The route in first list position is the shortest.
        return routes.get(0);
    }

}