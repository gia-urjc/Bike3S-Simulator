package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a user who makes most of his decissions randomly.
 * This user always chooses the closest destination station and the shortest route to reach it.
 * Moreover, this type of user only leaves the system when he has tried to make a bike 
 * reservation in all the system's stations and he hasn't been able.
 *   
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_RANDOM)
public class UserRandom extends User {

    public UserRandom() {
        super();
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public Station determineStationToRentBike() {
    	List<Station> stations = new ArrayList(infraestructureManager.consultStations());
     List<Station> triedStations = getMemory().getStationsWithBikeReservationAttempts(getInstant());
     stations.removeAll(triedStations);
     int index = infraestructureManager.getRandom().nextInt(0, stations.size());
     return stations.get(index);
    }

    @Override
    public Station determineStationToReturnBike() {
        List<Station> stations = new ArrayList(infraestructureManager.consultStations());
        List<Station> triedStations = getMemory().getStationsWithSlotReservationAttempts(getInstant());
        stations.removeAll(triedStations);
        int index = infraestructureManager.getRandom().nextInt(0, stations.size());
        return stations.get(index);    
			}
		
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return infraestructureManager.getRandom().nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return infraestructureManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        int index = infraestructureManager.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}