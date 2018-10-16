package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
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
@UserType("USER_INFORMED")
public class UserInformed extends User {

    @UserParameters
    public class Parameters {

        private Parameters() {}
        
        /**
         * It is the number of times that the user will try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;


        @Override
        public String toString() {
            return "Parameters{" +
                    "minRentalAttempts=" + minRentalAttempts+
            '}';
        }

    }


    private Parameters parameters;

    public UserInformed(Parameters parameters, SimulationServices services, GeoPoint finalDestination, long seed) {
        super(services,finalDestination,seed);
        this.parameters = parameters;
    }

    //**********************************************
    //Decision related to reservations
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return false;
    }
    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return false;
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
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return false;
    }

    //**********************************************
    //decisions related to taking and leaving a bike
    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) 
            return true; 
        else return false;
     }

    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Station> recommendedStations = informationSystem.getStationsToRentBikeOrderedByDistance(this.getPosition());
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        GeoPoint destinationPlace = this.destinationPlace;
        List<Station> recommendedStations = informationSystem.getStationsToReturnBikeOrderedByDistance(destinationPlace);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    //**********************************************
    //decisions related to either go directly to the destination or going arround

    @Override
    public boolean decidesToGoToPointInCity() {
        return false;
    }

    @Override
    public GeoPoint getPointInCity() {
        return null;
    }

}
