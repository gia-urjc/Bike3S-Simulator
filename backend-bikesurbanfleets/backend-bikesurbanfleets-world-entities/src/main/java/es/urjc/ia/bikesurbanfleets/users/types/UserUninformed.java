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
 * This class represents a user who doesn't know anything about the state of the system.
 * This user always chooses the closest destination station and the shortest route to reach it.
 * This user decides to leave the system randomly when a reservation fails.
 *
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_UNINFORMED)
public class UserUninformed extends User {

    public class UserUninformedParameters {

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


        private UserUninformedParameters(){}

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

    private UserUninformedParameters parameters;

    public UserUninformed(UserUninformedParameters parameters) {
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

        Station destination = null;
        List<Station> stations;
        if(parameters.willReserve) {
            stations = systemManager.consultStationsWithoutBikeReservationAttemptOrdered(this, instant);
        }
        else {
            stations = systemManager.consultStationWithoutBikeRentAttempt(this);
        }

        int index = 0;
        while(index < stations.size()) {
            Station stationToCheck = stations.get(index);
            if(stationToCheck.getPosition().equals(this.getPosition())) {
                stations.remove(stationToCheck);
                break;
            }
            index++;
        }

        if(!stations.isEmpty()) {
            destination = stations.get(0);
        }

        return destination;
    }

    @Override
    public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutSlotReservationAttemptOrdered(this, instant);
        Station destination;

        if(instant == 23810) {
            System.out.println();
        }

        int index = 0;
        while(index < stations.size()) {
            Station stationToCheck = stations.get(index);
            if(stationToCheck.getPosition().equals(this.getPosition())) {
                stations.remove(stationToCheck);
                break;
            }
            index++;
        }

        if(!stations.isEmpty()) {
            destination = stations.get(0);
        }
        else{
            stations = systemManager.consultOrderedStationsByDistance(this);

            Station firstStation = stations.get(0);
            if(firstStation.getPosition().equals(this.getPosition())) {
                stations.remove(0);
            }
            destination = stations.get(0);
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
