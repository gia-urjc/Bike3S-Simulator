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
@AssociatedType(UserType.USER_INFORMED)
public class UserInformed extends User {

    public class UserInformedParameters {

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
        private int minReservationAttempts = infraestructureManager.getRandom().nextInt(2, 4);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructureManager.getRandom().nextInt(1, 3);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructureManager.getRandom().nextInt(3, 6);

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


        private UserInformedParameters(){}

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


    private UserInformedParameters parameters;

    public UserInformed(UserInformedParameters parameters, SimulationServices services) {
        super(services);
        this.parameters = parameters;
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return parameters.willReserve ?
                getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts : infraestructureManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return parameters.willReserve ?
                getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts : infraestructureManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return parameters.willReserve ?
                getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts : infraestructureManager.getRandom().nextBoolean();
    }

    @Override
    public StationInfo determineStationToRentBike() {
        List<StationInfo> recommendedStations = informationSystem.recommendToRentBikeByDistance(this.getPosition());
        StationInfo destination = null;
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
    public StationInfo determineStationToReturnBike() {
        List<StationInfo> recommendedStations = informationSystem.recommendToReturnBikeByDistance(this.getPosition());
        StationInfo destination = null;
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        else {
            recommendedStations= infraestructureManager.consultStations();
            int index = infraestructureManager.getRandom().nextInt(0, recommendedStations.size()-1);
            destination = recommendedStations.get(index);
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
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.bikeReservationPercentage;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.slotReservationPercentage;
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
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.reservationTimeoutPercentage;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.failedReservationPercentage;
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
