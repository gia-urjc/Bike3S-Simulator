package es.urjc.ia.bikesurbanfleets.users;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation.ReservationState;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation.ReservationType;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It saves information about negative facts, i. e., events which has not finally happened (failed reservations/rentals/rturns attempts).
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class UserMemory {
    
    public enum FactType {
        BIKE_RESERVATION_TIMEOUT, BIKE_FAILED_RESERVATION, BIKES_UNAVAILABLE, SLOTS_UNAVAILABLE,
        SLOT_RESERVATION_TIMEOUT, SLOT_FAILED_RESERVATION
    }
    
    /**
     * Times that a user has tried to reserve a bike and has not been able to.
     */
    private int bikeReservationAttemptsCounter;
    
    /**
     * Times that a user's bike reservation has expired (before renting the bike).  
     */
    private int bikeReservationTimeoutsCounter;

    /**
     * Times that a user has tried to reserve a slot and has not been able to.
     */
    private int slotReservationAttemptsCounter;
    
    /**
     * Times that a user's slot reservation has expired (before renting the bike).  
     */
    private int slotReservationTimeoutsCounter;

    /**
     * Times that a user has tried to rent a bike and has not been able to.
     */
    private int rentalAttemptsCounter;
    
    /**
     * Times that a user has tried to return the bike and has not been able to.
     */
    private int returnAttemptsCounter;

    private User user;

    private List<Station> stationsWithRentalFailedAttempts;
    private List<Station> stationsWithReturnFailedAttemptss;
    private List<Reservation> reservations;
    private List<GeoRoute> routesTraveledByBike;
    private List<GeoRoute> walkedRoutes; 
    private double distanceTraveledByBike;
    private double walkedDistance;
  
    public UserMemory(User user) {
        this.bikeReservationAttemptsCounter = 0; 
        this.bikeReservationTimeoutsCounter = 0;
        this.rentalAttemptsCounter = 0;
        this.returnAttemptsCounter = 0;
        this.user = user;
        this.stationsWithRentalFailedAttempts = new ArrayList<>();
        this.stationsWithReturnFailedAttemptss = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.routesTraveledByBike = new ArrayList<>();
        this.walkedRoutes = new ArrayList<>();
        this.distanceTraveledByBike = 0;
        this.walkedDistance = 0;
    }
    
    public List<Reservation> getReservations() {
    	return reservations;
    }

    public int getReservationAttemptsCounter() {
        return bikeReservationAttemptsCounter;
    }

    public int getReservationTimeoutsCounter() {
        return bikeReservationTimeoutsCounter;
    }

    public int getRentalAttemptsCounter() {
        return rentalAttemptsCounter;
        
    }

    public int getReturnAttemptsCounter() {
        return returnAttemptsCounter;
    }

    public List<Station> getStationsWithRentalFailedAttempts() {
        return this.stationsWithRentalFailedAttempts;
    }

    public List<Station> getStationsWithReturnFailedAttempts() {
        return this.stationsWithReturnFailedAttemptss;
    }
    
    public List<GeoRoute> getRoutesTraveledByBike() {
    	return routesTraveledByBike;
    }
    
    public List<GeoRoute> getWalkedRoutes() {
    	return walkedRoutes;
    }
    
    public double getDistanceTraveledByBike() {
    	return distanceTraveledByBike;
    }
    
    public double getWalkedDistance() {
    	return walkedDistance;
    }
    
    public void addRouteTraveledByBike(GeoRoute route) {
    	routesTraveledByBike.add(route);
    }
    
    public void addWalkedRoute(GeoRoute route) {
    	walkedRoutes.add(route);
    }
    
    public void setDistanceTraveledByBike(double distance) {
    	distanceTraveledByBike = distance;
    }
    
    public void setWalkedDistance(double distance) {
    	walkedDistance = distance;
    }
    
    public void update(FactType fact) throws IllegalArgumentException {
        switch(fact) {
            case BIKE_RESERVATION_TIMEOUT: bikeReservationTimeoutsCounter++;
            break;
            case BIKE_FAILED_RESERVATION: bikeReservationAttemptsCounter++;
            break;
            case SLOT_RESERVATION_TIMEOUT: slotReservationTimeoutsCounter++;
            break;
            case SLOT_FAILED_RESERVATION: slotReservationAttemptsCounter++;
            break;
            case BIKES_UNAVAILABLE:
                rentalAttemptsCounter++;
                stationsWithRentalFailedAttempts.add(user.getDestinationStation());
            break;
            case SLOTS_UNAVAILABLE:
                returnAttemptsCounter++;
                stationsWithReturnFailedAttemptss.add(user.getDestinationStation());
            break;
            default: throw new IllegalArgumentException(fact.toString() + "is not defined in update method");
        }
    }
    
    /**
    * It obtains the stations for which a user has tried to make a bike reservation in an specific moment.
    * @param timeInstant it is the moment at which he has decided he wants to reserve a bike
    * and he has been trying it.
    * @return a list of stations for which the bike reservation has failed because of unavailable bikes.
    */    
    public List<Station> getStationsWithBikeReservationAttempts(int timeInstant) {
        return reservations.stream()
                .filter(reservation -> reservation.getType() == ReservationType.BIKE)
                .filter(reservation -> reservation.getState() == ReservationState.FAILED)
                .filter(reservation -> reservation.getStartInstant() == timeInstant)
                .map(Reservation::getStation)
                .collect(Collectors.toList());
    }
    
    public List<Station> getStationsWithSlotReservationAttempts(int timeInstant) {
        return reservations.stream()
                .filter(reservation -> reservation.getType() == ReservationType.SLOT)
                .filter(reservation -> reservation.getState() == ReservationState.FAILED)
                .filter(reservation -> reservation.getStartInstant() == timeInstant)
                .map(Reservation::getStation)
                .collect(Collectors.toList());
    }
    
    public double getTimeRidingABike() {
    	return distanceTraveledByBike / user.getCyclingVelocity();
    }
    
    public double getTimeWalking() {
    	return walkedDistance / user.getWalkingVelocity();
    }

}
