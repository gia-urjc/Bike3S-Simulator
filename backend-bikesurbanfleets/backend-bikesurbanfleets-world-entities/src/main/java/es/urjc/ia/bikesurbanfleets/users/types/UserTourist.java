package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a tourist, so this user, after renting a bike, cycles to 
 * some place in the city in order to visit it.
 * Then, this user never decides to return directly the bike just after renting it.  
 * This type of user always chooses the longest route when he has rented a bike.
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_TOURIST)
public class UserTourist extends User {
    
    public class UserTouristParameters {
        /**
         * It indicates the size of the set of stations closest to the user within which the
         * destination will be chossen randomly.
         */
        private final int SELECTION_STATIONS_SET = 3;

        /**
         * It is the maximum time in seconds until which the user will decide to continue walking
         * or cycling towards the previously chosen station witohout making a new reservation
         * after a reservation timeout event has happened.
         */
        private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;

        /**
         * It is the place the tourist wants to visit after
         * renting
         * a b
         * ike.
         */
        private GeoPoint touristDestination;

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
        private int bikeReservationPercentage;

        /**
         * It determines the rate with which the user will reserve a slot.
         */
        private int slotReservationPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after a  timeout event happens.
         */
        private int reservationTimeoutPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after he hasn't been able to make a reservation.
         */
        private int failedReservationPercentage;

        private UserTouristParameters() {}

        @Override
        public String toString() {
            return "UserTouristParameters{" +
                    "SELECTION_STATIONS_SET=" + SELECTION_STATIONS_SET +
                    ", MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", touristDestination=" + touristDestination +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentalAttempts=" + minRentalAttempts +
                    ", bikeReservationPercentage=" + bikeReservationPercentage +
                    ", slotReservationPercentage=" + slotReservationPercentage +
                    ", reservationTimeoutPercentage=" + reservationTimeoutPercentage +
                    ", failedReservationPercentage=" + failedReservationPercentage +
                    '}';
        }
    }

    private UserTouristParameters parameters;

    public UserTourist(UserTouristParameters parameters) {
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
        return getMemory().getCounterRentingAttempts() == parameters.minRentalAttempts ? true : false;
    }
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        Station destination = null;
        
        if (!stations.isEmpty()) {
                List<Station> recommendedStations = systemManager.getRecommendationSystem()
                .recommendByLinearDistance(this.getPosition(), stations);
                
                if (recommendedStations.get(0).getPosition().equals(this.getPosition( ))) {
                	recommendedStations.remove(0);
                }
                
                List<Station> nearestStations = new ArrayList<>();
                
                int end = parameters.SELECTION_STATIONS_SET < recommendedStations.size() 
                    ? parameters.SELECTION_STATIONS_SET : recommendedStations.size();


                for(int i = 0; i < end; i++) {
                    nearestStations.add(recommendedStations.get(i));
                }
         
                int index = systemManager.getRandom().nextInt(0, parameters.SELECTION_STATIONS_SET - 1);
                destination = nearestStations.get(index);
        }
        
        return destination;
    }
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        
        if (stations.isEmpty()) {
            stations = new ArrayList<>(systemManager.consultStations());
        }

        List<Station> recommendedStations = systemManager.getRecommendationSystem()
                .recommendByLinearDistance(this.getPosition(), stations);
        
        if (recommendedStations.get(0).getPosition().equals(this.getPosition( ))) {
        	recommendedStations.remove(0);
        }
        
        List<Station> nearestStations = new ArrayList<>();
        
        for(int i = 0; i < parameters.SELECTION_STATIONS_SET; i++) {
            nearestStations.add(recommendedStations.get(i));
        }
         
        int index = systemManager.getRandom().nextInt(0, parameters.SELECTION_STATIONS_SET - 1);
        return nearestStations.get(index);
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
     return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : systemManager.getRandom().nextBoolean();
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
        return parameters.touristDestination;
    }

    @Override
    public boolean decidesToReturnBike() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.failedReservationPercentage ? true : false;
    }
    
    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        if(!hasBike()) {
            return routes.get(0);    
        }
        else {
            return routes.get(routes.size() - 1);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }

}