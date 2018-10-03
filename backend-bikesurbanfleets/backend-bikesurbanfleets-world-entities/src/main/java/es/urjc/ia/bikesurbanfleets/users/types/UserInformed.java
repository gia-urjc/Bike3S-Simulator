package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
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

        /**
         * User destination place
         */
        private GeoPoint destinationPlace;

        /**
         * It is the maximum time in seconds until which the user will decide to continue walking
         * or cycling towards the previously chosen station witohout making a new reservation
         * after a reservation timeout event has happened.
         */
        private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;

        /**
         * It is the number of times that the user musts try to make a bike reservation before
         * deciding to leave the system.
         */
        private int minReservationAttempts = infraestructure.getRandom().nextInt(2, 4);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructure.getRandom().nextInt(1, 3);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructure.getRandom().nextInt(3, 6);

        /**
         * It determines the rate with which the user will reserve a bike.
         */
        private int bikeReservationPercentage = 0;

        /**
         * It determines the rate with which the user will reserve a slot.
         */
        private int slotReservationPercentage = 0;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after a  timeout event happens.
         */
        private int reservationTimeoutPercentage = 0;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after he hasn't been able to make a reservation.
         */
        private int failedReservationPercentage = 0;

        /**
         * It determines if the user will reserve or not
         */
        private boolean willReserve = false;


        private Parameters(){}

        @Override
        public String toString() {
            return "UserUninformedParameters{" +
                    "MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentalAttempts=" + minRentalAttempts +
                    ", bikeReservationPercentage=" + bikeReservationPercentage +
                    ", slotReservationPercentage=" + slotReservationPercentage +
                    ", reservationTimeoutPercentage=" + reservationTimeoutPercentage +
                    ", failedReservationPercentage=" + failedReservationPercentage +
                    ", willReserve=" + willReserve +
                    '}';
        }
    }


    private Parameters parameters;

    public UserInformed(Parameters parameters, SimulationServices services) {
        super(services);
        this.parameters = parameters;
        this.destinationPlace = parameters.destinationPlace;
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return parameters.willReserve ?
                getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts : infraestructure.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return parameters.willReserve ?
                getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts : infraestructure.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return parameters.willReserve ?
                getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts : infraestructure.getRandom().nextBoolean();
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
        System.out.println(parameters.destinationPlace);
        GeoPoint destinationPlace = parameters.destinationPlace;
        List<Station> recommendedStations = informationSystem.getStationsToReturnBikeOrderedByDistance(destinationPlace);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.bikeReservationPercentage;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.slotReservationPercentage;
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
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.reservationTimeoutPercentage;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.failedReservationPercentage;
    }

    @Override
    public GeoRoute determineRoute() throws Exception{
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        if(routes != null) {
            int index = infraestructure.getRandom().nextInt(0, routes.size());
            return routes.get(index);
        }
        else {
            return null;
        }
    }

}
