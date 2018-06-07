package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserType;

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
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public StationInfo determineStationToRentBike() {
        List<StationInfo> recommendedStations = informationSystem.recommendToRentBikeByDistance(this.getPosition());
        StationInfo destination = null;
        if (!recommendedStations.isEmpty()) {
             List<StationInfo> nearestStations = new ArrayList<>();
	                
             int end = parameters.SELECTION_STATIONS_SET < recommendedStations.size() 
                 ? parameters.SELECTION_STATIONS_SET : recommendedStations.size();
	                
             for(int i = 0; i < end; i++) {
                 nearestStations.add(recommendedStations.get(i));
             }
	         
	            int index = infraestructureManager.getRandom().nextInt(0, end-1);
	            destination = nearestStations.get(index);
        }
        return destination;
    }
    
    /**
     * It randomly chooses a station among the pre-established number of nearest stations.
     */
    @Override
    public StationInfo determineStationToReturnBike() {
    	List<StationInfo> recommendedStations = informationSystem.recommendToReturnBikeByDistance(this.getPosition());
     if (recommendedStations.isEmpty()) {
        	recommendedStations = infraestructureManager.consultStations();
     }
     int end = parameters.SELECTION_STATIONS_SET < recommendedStations.size() 
         ? parameters.SELECTION_STATIONS_SET : recommendedStations.size();
       List<StationInfo> nearestStations = new ArrayList<>();
       for(int i = 0; i < end; i++) {
            nearestStations.add(recommendedStations.get(i));
       }
       int index = infraestructureManager.getRandom().nextInt(0, end-1);
       return nearestStations.get(index);
    }

    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
     return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : infraestructureManager.getRandom().nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReservationPercentage ? true : false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
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
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
        return percentage < parameters.reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = infraestructureManager.getRandom().nextInt(0, 100);
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