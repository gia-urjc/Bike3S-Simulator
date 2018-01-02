package es.urjc.ia.bikesurbanfleets.entities.users;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class UserMemory {
    
    public enum FactType {
        BIKE_RESERVATION_TIMEOUT, BIKE_FAILED_RESERVATION, BIKES_UNAVAILABLE
    }
    
    private int counterReservationAttempts;
    private int counterReservationTimeouts;
    private int counterRentingAttempts;
    
    public UserMemory() {
        this.counterReservationAttempts = 0; 
        this.counterReservationTimeouts = 0;
        this.counterRentingAttempts = 0;
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

    public void update(FactType fact) throws IllegalArgumentException {
        switch(fact) {
            case BIKE_RESERVATION_TIMEOUT: counterReservationTimeouts++;
            break;
            case BIKE_FAILED_RESERVATION: counterReservationAttempts++;
            break;
            case BIKES_UNAVAILABLE: counterRentingAttempts++;
            break;
            default: throw new IllegalArgumentException(fact.toString() + "is not defined in update method");
        }
    }

}
