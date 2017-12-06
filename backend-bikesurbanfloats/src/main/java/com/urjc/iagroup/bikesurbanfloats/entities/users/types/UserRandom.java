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
// TODO: add lost documentation
@AssociatedType(UserType.USER_RANDOM)
public class UserRandom extends User {

    public UserRandom() {
        super();
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }

    @Override
    public Station determineStationToRentBike(int instant) {
    	return systemManager.getRecommendationSystem().recommendByLinearDistance(this).get(0);
    	
/* TODO: delete this code after debuging the method
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        double minDistance = Double.MAX_VALUE;
        Station destination = null;
        for (Station currentStation: stations) {
            GeoPoint stationPosition = currentStation.getPosition();
            GeoPoint userPosition = getPosition();
            double distance = userPosition.distanceTo(stationPosition);
            if (!userPosition.equals(stationPosition) && distance < minDistance) {
                minDistance = distance;
                destination = currentStation;
            }
        }
        return destination; */
    }

    @Override
    public Station determineStationToReturnBike(int instant) {
    	return systemManager.getRecommendationSystem().recommendByLinearDistance(this).get(0);

/* TODO: delete this code after debuging it 
        List<Station> stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);
        double minDistance = Double.MAX_VALUE;
        Station destination = null;
        for (Station currentStation : stations) {
            GeoPoint stationGeoPoint = currentStation.getPosition();
            GeoPoint userGeoPoint = getPosition();
            double distance = stationGeoPoint.distanceTo(userGeoPoint);
            if (!userGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
                minDistance = distance;
                destination = currentStation;
            }
        }
        return destination; */
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
        return systemManager.getRandom().nextBoolean();
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
        int index = systemManager.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}