package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

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

    public UserRandom(JsonObject userdef, SimulationServices services, long seed) {
        super(services, userdef, seed);
        //read specific parameters
        JsonObject jsonparameters = userdef.getAsJsonObject("userType").getAsJsonObject("parameters");
        if (jsonparameters != null) {
            throw new IllegalArgumentException("random user can not have parameters");
        } 
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return rando.nextBoolean();
    }

    @Override
    public Station determineStationToRentBike() {
    	List<Station> stations = infraestructure.consultStations();
    	List<Station> triedStations = getMemory().getStationsWithBikeReservationAttempts(getInstant());
    	stations.removeAll(triedStations);
        //Remove station if the user is in this station
        stations.removeIf(station -> station.getPosition().equals(this.getPosition()) && station.availableBikes() == 0);
        if(!stations.isEmpty()) {
            int index = rando.nextInt(0, stations.size());
            return stations.get(index);
        }
        //The user wants a bike but tried in all stations
        else {
            List<Station> allStations = infraestructure.consultStations();
            allStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
            int index = rando.nextInt(0, allStations.size());
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
        int index = rando.nextInt(0, stations.size());
        return stations.get(index);
    }
		
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return rando.nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return rando.nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return rando.nextBoolean();
    }

       //**********************************************
    //decisions related to either go directly to the destination or going arround

    @Override
    public boolean decidesToGoToPointInCity() {
         return rando.nextBoolean();
   }

    @Override
    public GeoPoint getPointInCity() {
        return infraestructure.generateBoundingBoxRandomPoint(rando);
    }


}