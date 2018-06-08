package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public UserUninformed(SimulationServices services) {
        super(services);
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public StationInfo determineStationToRentBike() {
        StationInfo destination = null;
        List<StationInfo> stations = infraestructure.consultStations();
        List<Station> triedStations = getMemory().getStationsWithBikeReservationAttempts(getInstant());
        stations.removeAll(triedStations);
        
        Comparator<StationInfo> criteria = services.getStationComparator().byDistance(this.getPosition());
        stations.stream().sorted(criteria).collect(Collectors.toList());
        if (!stations.isEmpty()) {
        	destination = stations.get(0);
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> stations = new ArrayList(infraestructure.consultStations());
        List<Station> triedStations = getMemory().getStationsWithSlotReservationAttempts(getInstant());
        stations.removeAll(triedStations);
        
        Comparator<StationInfo> criteria = services.getStationComparator().byDistance(this.getPosition());
        stations.stream().sorted(criteria).collect(Collectors.toList());
        if (!stations.isEmpty()) {
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
        return infraestructure.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return infraestructure.getRandom().nextBoolean();
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
        int index = infraestructure.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}
