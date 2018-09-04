package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

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
@UserType("USER_RANDOM")
public class UserRandom extends User {

    public UserRandom(SimulationServices services) {
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
    public Station determineStationToRentBike() {
    	List<Station> stations = infraestructure.consultStations();
    	List<Station> triedStations = getMemory().getStationsWithBikeReservationAttempts(getInstant());
    	stations.removeAll(triedStations);
        //Remove station if the user is in this station
        stations.removeIf(station -> station.getPosition().equals(this.getPosition()) && station.availableBikes() == 0);
        if(!stations.isEmpty()) {
            int index = infraestructure.getRandom().nextInt(0, stations.size());
            return stations.get(index);
        }
        //The user wants a bike but tried in all stations
        else {
            List<Station> allStations = infraestructure.consultStations();
            allStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
            int index = infraestructure.getRandom().nextInt(0, allStations.size());
            return allStations.get(index);
        }
    }

    @Override
    public Station determineStationToReturnBike() {
        List<Station> stations = infraestructure.consultStations();
        List<Station> triedStations = getMemory().getStationsWithSlotReservationAttempts(getInstant());
        stations.removeAll(triedStations);
        //Remove station if the user is in this station
        stations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        int index = infraestructure.getRandom().nextInt(0, stations.size());
        return stations.get(index);
    }
		
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return infraestructure.getRandom().nextBoolean();
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
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public GeoRoute determineRoute() throws Exception{
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        if(routes != null) {
            int index = infraestructure.getRandom().nextInt(0, routes.size());
            return routes != null ? routes.get(index) : null;
        }
        else {
    	    return null;
        }
    }

}