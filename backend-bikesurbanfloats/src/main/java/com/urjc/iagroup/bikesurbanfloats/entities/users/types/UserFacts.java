package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

import java.util.List;
import java.util.ArrayList;

@AssociatedType(UserType.USER_FACTS)
public class UserFacts extends User {
	private final int SELECTION_STATIONS_SET = 3;
	private static int maxReservationAttempts;
	private static int maxReservationTimeouts;
	private static int maxRentingAttempts;
	
    public UserFacts() {
        super();
    }
    
    public static void setMaximums(int reservationAttempts, int reservationTimeouts, int rentingAttempts) {
    	maxReservationAttempts = reservationAttempts;
    	maxReservationTimeouts = reservationTimeouts;
    	maxRentingAttempts = rentingAttempts;
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == maxReservationTimeouts ? true : false;
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getCounterReservationAttempts() == maxReservationAttempts ? true : false;
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == maxRentingAttempts ? true : false;
    }

    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        List<Station> nearestStations = new ArrayList<>();

        for(int i = 0; i < SELECTION_STATIONS_SET; i++) {
            double minDistance = Double.MAX_VALUE;
        	for (Station currentStation: stations) {
            GeoPoint stationPosition = currentStation.getPosition();
            GeoPoint userPosition = getPosition();
            double distance = userPosition.distanceTo(stationPosition);
            if (!userPosition.equals(stationPosition) && distance < minDistance) {
                minDistance = distance;
                nearestStations.add(currentStation);
                stations.remove(currentStation);
            }
        	}
        }
        int index = systemManager.getRandom().nextInt(0, SELECTION_STATIONS_SET - 1);
        return nearestStations.get(index);
    }

    @Override
    public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        List<Station> nearestStations = new ArrayList<>();

        for(int i = 0; i < SELECTION_STATIONS_SET; i++) {
            double minDistance = Double.MAX_VALUE;
        	for (Station currentStation: stations) {
            GeoPoint stationPosition = currentStation.getPosition();
            GeoPoint userPosition = getPosition();
            double distance = userPosition.distanceTo(stationPosition);
            if (!userPosition.equals(stationPosition) && distance < minDistance) {
                minDistance = distance;
                nearestStations.add(currentStation);
                stations.remove(currentStation);
            }
        	}
        }
        int index = systemManager.getRandom().nextInt(0, SELECTION_STATIONS_SET - 1);
        return nearestStations.get(index);
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < Reservation.VALID_TIME ? false : true;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return systemManager.getRandom().nextBoolean();
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