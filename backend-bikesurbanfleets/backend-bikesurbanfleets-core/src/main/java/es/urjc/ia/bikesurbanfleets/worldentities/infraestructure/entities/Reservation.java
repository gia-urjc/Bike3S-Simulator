package es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import java.util.Objects;

/**
 * It can be a bike or slot reservation depending on its type property It can
 * represents a reservations at all its possible states: ACTIVE: the reservation
 * is valid at that moment FAILED: user has tried to make a reservation and it
 * hasn't been possible (there're no available bikes or solts) EXPIRED:
 * reservation has been made but timeout has happend SUCCESSFUL: user has
 * removed or returned his bike, so reservation has been resolved succesfully
 * (the reservation ceases to exist)
 *
 * @author IAgroup
 *
 */
public class Reservation implements Entity {

    public enum ReservationType {
        SLOT, BIKE
    }

    public enum ReservationState {
        ACTIVE, FAILED, EXPIRED, SUCCESSFUL
    }

    /**
     * It is the time during which a reservation is active.
     */
    public static int VALID_TIME;

    private int id;
    private int startInstant;  // instant when user makes the reservation
    private int endInstant;  // instant when reservation is resolved or expired
    private ReservationType type;
    private ReservationState state;
    private User user;
    private Station station;

    public static IdGenerator idgenerator;

    public static void resetIdGenerator() {
        idgenerator = new IdGenerator();
    }
    /**
     * It is the bike which the user reserves or the rented bike which the user
     * wants to return.
     */
    private Bike bike;

    /**
     * AThis is the constructor for reserving bikes: if sucess is true the
     * reservation has been sucessfull otherwise not if no success, bike must be
     * null, otherwise nor
     *
     */
    public Reservation(int startInstant, User user, ReservationType type, Station station, Bike bike, boolean sucess) {
        if (type != ReservationType.BIKE || (sucess && bike == null) || (!sucess && bike != null)) {
            throw new RuntimeException("invalid cration of reservation");
        }
        this.id = idgenerator.next();
        this.startInstant = startInstant;
        this.type = ReservationType.BIKE;
        if (sucess) {
            this.state = ReservationState.ACTIVE;
            this.endInstant = -1; // reservation has'nt ended
        } else {
            this.state = ReservationState.FAILED;
            this.endInstant = startInstant;
        }
        this.user = user;
        this.station = station;
        this.bike = bike;
    }

    /**
     * AThis is the constructor for reserving slots: if sucess is true the
     * reservation has been sucessfull otherwise not
     *
     */
    public Reservation(int startInstant, User user, ReservationType type, Station station, boolean sucess) {
        if (type != ReservationType.SLOT) {
            throw new RuntimeException("invalid cration of reservation");
        }
        this.id = idgenerator.next();
        this.startInstant = startInstant;
        this.type = ReservationType.SLOT;
        if (sucess) {
            this.state = ReservationState.ACTIVE;
            this.endInstant = -1; // reservation has'nt ended
        } else {
            this.state = ReservationState.FAILED;
            this.endInstant = startInstant;
        }
        this.user = user;
        this.station = station;
        this.bike = null;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getStartInstant() {
        return startInstant;
    }

    public int getEndInstant() {
        return endInstant;
    }

    public ReservationType getType() {
        return type;
    }

    public ReservationState getState() {
        return state;
    }

    public User getUser() {
        return user;
    }

    public Station getStation() {
        return station;
    }

    public Bike getBike() {
        return bike;
    }

    /**
     * Set reservation state to expired and updates reservation end instant
     */
    public void expire(int instant) {
        if (this.state != ReservationState.ACTIVE) {
            throw new RuntimeException("Invalid programm state:expire ");
        }
        this.state = ReservationState.EXPIRED;
        this.endInstant = instant;
    }

    /**
     * Set reservation state to successful and updates reservation end instant
     *
     * @param endInstant: it is the time instant when user removes or returns a
     * bike with a previous bike or slot reservation, respectively
     */
    public void resolve(int endInstant) {
        if (this.state != ReservationState.ACTIVE) {
            throw new RuntimeException("Invalid programm state:resolve ");
        }
        this.state = ReservationState.SUCCESSFUL;
        this.endInstant = endInstant;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Reservation other = (Reservation) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.startInstant != other.startInstant) {
            return false;
        }
        if (this.endInstant != other.endInstant) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (!Objects.equals(this.user.getId(), other.user.getId())) {
            return false;
        }
        if (!Objects.equals(this.station.getId(), other.station.getId())) {
            return false;
        }
        if (!Objects.equals(this.bike.getId(), other.bike.getId())) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        String result = this.getClass().getSimpleName()+" : | Id: " + getId();
        if (getBike()!= null) result += " | Bike: " + getBike().getId();
        else result += " | Bike: null";
        result += " | Station: " + getStation().getId();
        result += " | User: " + getUser().getId();
        result += " | Type: " + getType();
        result += " | State: " + getState();
        result += " | Starttime: " + getStartInstant();
        result += " | Endtime: " + getEndInstant();
        return result;
    }
 
}
