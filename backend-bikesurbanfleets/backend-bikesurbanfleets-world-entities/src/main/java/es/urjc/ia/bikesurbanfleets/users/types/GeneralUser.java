/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.users.types;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;
import java.lang.reflect.Field;

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
@UserType("USER_GENERAL")
public class GeneralUser extends User {

    @UserParameters
    public class Parameters {

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
        private int minReservationAttempts = rando.nextInt(2, 4);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = rando.nextInt(1, 3);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = rando.nextInt(3, 6);

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

        //default constructor used if no parameters are specified
        private Parameters() {
        }


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

   public GeneralUser(JsonObject userdef, SimulationServices services, long seed) throws Exception{
        super(services, userdef, seed);
        //***********Parameter treatment*****************************
        //if this user has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the user
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new Parameters();
        getParameters(userdef, this.parameters);
     }


    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return parameters.willReserve ?
                getMemory().getReservationTimeoutsCounter() >= parameters.minReservationTimeouts : rando.nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return parameters.willReserve ?
                getMemory().getReservationAttemptsCounter() >= parameters.minReservationAttempts : rando.nextBoolean();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return parameters.willReserve ?
                getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts : rando.nextBoolean();
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

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        int percentage = rando.nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.bikeReservationPercentage;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return parameters.willReserve && arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = rando.nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.slotReservationPercentage;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = rando.nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.reservationTimeoutPercentage;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = rando.nextInt(0, 100);
        return parameters.willReserve && percentage < parameters.failedReservationPercentage;
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
