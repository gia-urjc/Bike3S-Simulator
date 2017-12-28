package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;

import java.util.List;

/**
 * This class represents a employee, i.e., a user who uses the bike as a public transport 
 * in order to arrive at work.
 * Then, this user always decides the destination station just after renting the bike 
 * in order to arrive at work as soon as possible.
 * Moreover, he always chooses both the closest origin station to himself and the closest destination 
 * station to his work. Also, he always chooses the shortest routes to get the stations.
 * Also, this type of user always determines a new destination station after 
 * a reservation failed attempt and always decides to continue to the previously chosen 
 * station after a timeout event with the intention of losing as little time as possible.
 * And, of course, he never leaves the system as he needs to ride on bike in order to arrive at work. 
 *   
 * @author IAgroup
  */
@AssociatedType(UserType.USER_EMPLOYEE)
public class UserEmployee extends User {

    public class UserEmployeeParameters {

        /**
         * It is the street of the company where the user works.
         */
        private GeoPoint companyStreet;

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
        private int minReservationAttempts = systemManager.getRandom().nextInt(3, 6);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = systemManager.getRandom().nextInt(3, 6);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentingAttempts = systemManager.getRandom().nextInt(4, 7);

        @Override
        public String toString() {
            return "UserEmployeeParameters{" +
                    "companyStreet=" + companyStreet +
                    ", bikeReservationPercentage=" + bikeReservationPercentage +
                    ", slotReservationPercentage=" + slotReservationPercentage +
                    ", MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentingAttempts=" + minRentingAttempts +
                    '}';
        }

        private UserEmployeeParameters() {}
    }
    private UserEmployeeParameters parameters;
    
    public UserEmployee(UserEmployeeParameters parameters) {
        super();
        this.parameters = parameters;
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getCounterReservationAttempts() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == parameters.minRentingAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        Station destination = null;
        
        if (!stations.isEmpty()) {
            destination = systemManager.getRecommendationSystem().recommendByLinearDistance(this
                    .getPosition(), stations).get(0);
        }
        
        return destination; 
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);

        if (stations.isEmpty()) {
            stations = systemManager.consultStations();  
        }
        
        return systemManager.getRecommendationSystem().recommendByLinearDistance(this
                .getPosition(), stations).get(0); 
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
     return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReservationPercentage ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
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
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        // The route in first list position is the shortest.
        return routes.get(0);
    }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
}