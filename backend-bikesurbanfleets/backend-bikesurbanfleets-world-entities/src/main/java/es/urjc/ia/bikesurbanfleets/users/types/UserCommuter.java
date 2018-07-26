package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
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
@AssociatedType("USER_COMMUTER")
public class UserCommuter extends User {

    public class UserParameters {

        /**
         * Place where user will go to take return the bike
         */
        private GeoPoint destinationPlace;

        /**
         * It determines the rate with which the user will reserve a bike.
         */
        private int bikeReservationPercentage;

        /**
         * It determines the rate with which the user will reserve a slot.
         */
        private int slotReservationPercentage;

        /**
         * It is the time in seconds until which the user will decide to continue walking
         * or cycling towards the previously chosen station without making a new reservation
         * after a reservation timeout event has happened.
         */
        private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;


        /**
         * It is the number of times that the user musts try to make a bike reservation before
         * deciding to leave the system.
         */
        private int minReservationAttempts = infraestructure.getRandom().nextInt(3, 6);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructure.getRandom().nextInt(3, 6);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructure.getRandom().nextInt(4, 7);

        private double cyclingVelocity;

        @Override
        public String toString() {
            return "UserEmployeeParameters{" +
                    ", bikeReservationPercentage=" + bikeReservationPercentage +
                    ", slotReservationPercentage=" + slotReservationPercentage +
                    ", MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentalAttempts=" + minRentalAttempts +
                    '}';
        }

        private UserParameters() {}

    }
    private UserParameters parameters;
    
    public UserCommuter(UserParameters parameters, SimulationServices services) {
        super(services);
        this.parameters = parameters;
        if(parameters.cyclingVelocity != 0) {
            this.cyclingVelocity = parameters.cyclingVelocity;
        }
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike() {
        List<Station> recommendedStations = informationSystem.getStationsToRentBikeOrderedByDistance(this.getPosition());
        Station destination = null;
        //Remove station if the user is in this station
        recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition())  && station.availableBikes() == 0);
        if (!recommendedStations.isEmpty()) {
        destination = recommendedStations.get(0);
        }
        return destination;
    }
        
    @Override
     public Station determineStationToReturnBike() {
        List<Station> recommendedStations = informationSystem.getStationsToReturnBikeOrderedByDistance(parameters.destinationPlace);
        Station destination = null;
        //Remove station if the user is in this station
        recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
     return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReservationPercentage ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.slotReservationPercentage ? true : false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        // TODO: check it
        System.out.println("This user mustn't cycle to a place which isn't a station");
        return null;
    }

    @Override
    public boolean decidesToReturnBike() {
        return true;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
            return true;
        }
    
    @Override
    public GeoRoute determineRoute() throws Exception{
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        return routes != null ? routes.get(0) : null;
   }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
}