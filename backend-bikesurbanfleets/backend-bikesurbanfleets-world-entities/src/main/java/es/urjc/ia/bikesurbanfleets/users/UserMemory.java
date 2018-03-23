package es.urjc.ia.bikesurbanfleets.users;

import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class UserMemory {
    
    public enum FactType {
        BIKE_RESERVATION_TIMEOUT, BIKE_FAILED_RESERVATION, BIKES_UNAVAILABLE, SLOTS_UNAVAILABLE
    }
    
    private int counterReservationAttempts;
    private int counterReservationTimeouts;
    private int counterRentingAttempts;
    private int counterSlotDevolutionAttempts;

    private User user;

    private List<Station> stationsWithRentFailure;
    private List<Station> stationsWithSlotDevolutionFail;
    
    public UserMemory(User user) {
        this.counterReservationAttempts = 0; 
        this.counterReservationTimeouts = 0;
        this.counterRentingAttempts = 0;
        this.counterSlotDevolutionAttempts = 0;
        this.user = user;
        this.stationsWithRentFailure = new ArrayList<>();
        this.stationsWithSlotDevolutionFail = new ArrayList<>();
    }

    public int getCounterReservationAttempts() {
        return counterReservationAttempts;
    }

    public int getCounterReservationTimeouts() {
        return counterReservationTimeouts;
    }

    public int getCounterRentingAttempts() {
        return counterRentingAttempts;
    }

    public int getCounterSlotDevolutionAttempts() {
        return counterSlotDevolutionAttempts;
    }

    public List<Station> getStationsWithRentFailure() {
        return this.stationsWithRentFailure;
    }

    public List<Station> getStationsWithSlotDevolutionFail() {
        return this.stationsWithSlotDevolutionFail;
    }

    public void update(FactType fact) throws IllegalArgumentException {
        switch(fact) {
            case BIKE_RESERVATION_TIMEOUT: counterReservationTimeouts++;
            break;
            case BIKE_FAILED_RESERVATION: counterReservationAttempts++;
            break;
            case BIKES_UNAVAILABLE:
                counterRentingAttempts++;
                stationsWithRentFailure.add(user.getDestinationStation());
            break;
            case SLOTS_UNAVAILABLE:
                counterSlotDevolutionAttempts++;
                stationsWithSlotDevolutionFail.add(user.getDestinationStation());
            break;
            default: throw new IllegalArgumentException(fact.toString() + "is not defined in update method");
        }
    }

}
