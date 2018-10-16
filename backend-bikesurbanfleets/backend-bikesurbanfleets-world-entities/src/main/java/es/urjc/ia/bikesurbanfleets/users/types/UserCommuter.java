package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.List;

/**
 * This class represents a user (employee, student, etc) who uses the bike as a public transport 
 * in order to arrive at his destination place (work, university, etc).
 * Then, this user always decides the destination station just after renting the bike 
 * in order to arrive at work, university,... as soon as possible.
 * Moreover, he always chooses both the closest origin station to himself and the closest 
 * station to his destination. Also, he always chooses the shortest routes to get the stations.
 * Also, this type of user always determines a new destination station after 
 * a reservation failed attempt and always decides to continue to the previously chosen 
 * station after a timeout event with the intention of losing as little time as possible.
 * And, of course, he never leaves the system as he needs to ride on bike in order to arrive at work/university. 
 *   
 * @author IAgroup
  */
@UserType("USER_COMMUTER")
public class UserCommuter extends User {

    @UserParameters
    public class Parameters {

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;

        private double cyclingVelocity;

        @Override
        public String toString() {
            return "UserEmployeeParameters{" +
                    ", minRentalAttempts=" + minRentalAttempts +
                    '}';
        }

        private Parameters() {}

    }
    private Parameters parameters;
    
    public UserCommuter(Parameters parameters, SimulationServices services, GeoPoint finalDestination, long seed) {
        super(services,finalDestination,seed);
        this.parameters = parameters;
        if(parameters.cyclingVelocity != 0) {
            this.cyclingVelocity = parameters.cyclingVelocity;
        }
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
        List<Station> recommendedStations = informationSystem.getStationsToRentBikeOrderedByDistance(this.getPosition());
        Station destination = null;
        //Remove station if the user is in this station
    //    recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition())  && station.availableBikes() == 0);
        if (!recommendedStations.isEmpty()) {
        destination = recommendedStations.get(0);
        }
        return destination;
    }
        
    @Override
     public Station determineStationToReturnBike() {
        List<Station> recommendedStations = informationSystem.getStationsToReturnBikeOrderedByDistance(destinationPlace);
        Station destination = null;
        //Remove station if the user is in this station
    //    recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
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