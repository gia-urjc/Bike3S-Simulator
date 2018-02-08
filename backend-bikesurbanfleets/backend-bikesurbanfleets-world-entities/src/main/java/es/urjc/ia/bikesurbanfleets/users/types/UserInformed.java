package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
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
        private int minReservationAttempts = systemManager.getRandom().nextInt(2, 4);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = systemManager.getRandom().nextInt(1, 3);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = systemManager.getRandom().nextInt(3, 6);

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

    public UserInformed(UserInformedParameters parameters) {
        super();
        this.parameters = parameters;
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return parameters.willReserve ?
                getMemory().getCounterReservationTimeouts() == parameters.minReservationTimeouts : systemManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return parameters.willReserve ?
                getMemory().getCounterReservationAttempts() == parameters.minReservationAttempts : systemManager.getRandom().nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return parameters.willReserve ?
                getMemory().getCounterRentingAttempts() == parameters.minRentalAttempts : systemManager.getRandom().nextBoolean();
    }

    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations;
        if(parameters.willReserve) {
            stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        } else {
            stations = systemManager.consultStationsWithoutBikeRentAttempt(this);
        }

        Station destination = null;

        if (!stations.isEmpty()) {
            List<Station> recommendedStations = systemManager.getRecommendationSystem()
                    .recommendToRentBikeByDistance(this.getPosition(), stations);

            if (!recommendedStations.isEmpty()) {
                destination = recommendedStations.get(0);
            }
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike(int instant) {
        List<Station> stations;

        if(parameters.willReserve) {
            stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);
        } else {
            stations = systemManager.consultStationsWithoutSlotDevolutionAttempt(this);
        }

        List<Station> recommendedStations;
        Station destination;

        if (stations.isEmpty()) {
            stations = new ArrayList<Station>(systemManager.consultStations());
        }

        recommendedStations = systemManager.getRecommendationSystem()
                .recommendToReturnBikeByDistance(this.getPosition(), stations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        else {
            recommendedStations= systemManager.consultStations();
            int index = systemManager.getRandom().nextInt(0, recommendedStations.size()-1);
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
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.bikeReservationPercentage;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.slotReservationPercentage;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.reservationTimeoutPercentage;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.failedReservationPercentage;
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        int index = systemManager.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}
