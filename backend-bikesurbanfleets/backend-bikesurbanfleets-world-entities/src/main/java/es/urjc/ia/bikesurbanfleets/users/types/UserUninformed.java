package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.List;

/**
 * This class represents a user who doesn't know anything about the state of the system.
 * This user always chooses the closest destination station and the shortest route to reach it.
 * This user decides to leave the system randomly when a reservation fails if reservations are active
 *
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_UNINFORMED)
public class UserUninformed extends User {

    public UserUninformed() {
        super();
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public Station determineStationToRentBike(int instant) {

        Station destination = null;
        List<Station> stations;
        stations = systemManager.consultStationsWithoutBikeRentAttemptOrdered(this);

        int index = 0;
        while(index < stations.size()) {
            Station stationToCheck = stations.get(index);
            if(stationToCheck.getPosition().equals(this.getPosition())) {
                stations.remove(stationToCheck);
                break;
            }
            index++;
        }

        if(!stations.isEmpty()) {
            destination = stations.get(0);
        }

        return destination;
    }

    @Override
    public Station determineStationToReturnBike(int instant) {

        List<Station> stations;
        stations = systemManager.consultStationsWithoutSlotDevolutionAttemptOrdered(this);

        Station destination;

        int index = 0;
        while(index < stations.size()) {
            Station stationToCheck = stations.get(index);
            if(stationToCheck.getPosition().equals(this.getPosition())) {
                stations.remove(stationToCheck);
                break;
            }
            index++;
        }

        if(!stations.isEmpty()) {
            destination = stations.get(0);
        }
        else{
            stations = systemManager.consultOrderedStationsByDistance(this);

            Station firstStation = stations.get(0);
            if(firstStation.getPosition().equals(this.getPosition())) {
                stations.remove(0);
            }
            destination = stations.get(0);
        }
        return destination;

    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return false;
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
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return false;
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
