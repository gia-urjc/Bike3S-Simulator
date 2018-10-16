package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.List;

/**
 * This class represents a user who always follows the frist system recommendation with 
 * respect a proportion, i. e., he weighs what is the best recommendation (the most little 
 * value) in relation to the quotient between the distance from his position to the station 
 * and the number of available bikes or slots which it contains.   
 * This user always reserves bikes and slots at destination stations to ensure his service 
 * as he knows the stations he chooses as destination may not have too many bikes or slots. 
 * Moreover, he always chooses the shortest routes to get his destination.
 * 
 * @author IAgroup
 *
 */
@UserType("USER_REASONABLE")
public class UserDistanceResourcesRatio extends User {

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

    public UserDistanceResourcesRatio(Parameters parameters, SimulationServices services, GeoPoint finalDestination, long seed) {
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
        List<Station> recommendedStations = informationSystem.getStationsOrderedByDistanceSlotsRatio(this.getPosition());
        Station destination = null;
        //Remove station if the user is in this station
     //   recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
     public Station determineStationToReturnBike() {
        List<Station> recommendedStations = informationSystem.getStationsOrderedByDistanceSlotsRatio(destinationPlace);
        Station destination = null;
        //Remove station if the user is in this station
   //     recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()) && station.availableBikes() == 0);
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

     @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
}
