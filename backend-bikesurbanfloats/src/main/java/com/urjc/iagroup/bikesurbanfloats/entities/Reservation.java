package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.history.History;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricReservation;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

/**
 * It can be a bike or slot reservation depending on its type property
 * It can represents a reservations at all its possible states:
 *      ACTIVE: the reservation is valid at that moment
 *   FAILED: user has tried to make a reservation and it hasn't been possible (there're no available bikes or solts)
 *   EXPIRED: reservation has been made but timeout has happend
 *   SUCCESSFUL: user has removed or returned his bike, so reservation has been resolved succesfully (the reservation ceases to exist)  
 * @author IAgroup
 *
 */

@HistoryReference(HistoricReservation.class)
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

    private static IdGenerator idGenerator = new IdGenerator();

    private int id;
    private int startInstant;  // instant when user makes the reservation
    private int endInstant;  // instant when reservation is resolved or expired
    private ReservationType type;
    private ReservationState state;
    private User user;
    private Station station;
    /**
     * It is the bike which the user reserves or the rented bike which the user wants to return.
     */
    private Bike bike;

    /**
     * As it receives a bike param, it creates an active reservation 
     */
    
    public Reservation(int startInstant, ReservationType type, User user, Station station, Bike bike) {
        this.id = idGenerator.next();
        this.commonInit(startInstant, type, user, station, bike);
        this.endInstant = -1; // reservation has'nt ended
        this.state = ReservationState.ACTIVE;
        History.registerEntity(this);
    }
    
    /**
     * As it doesn't receive a bike param, it creates a failed reservation 
     */

    public Reservation(int startInstant, ReservationType type, User user, Station station) {
        this.id = idGenerator.next();
        this.commonInit(startInstant, type, user, station, null);
        this.endInstant = startInstant;
        this.state = ReservationState.FAILED;
        History.registerEntity(this);
    }

    private void commonInit(int startInstant, ReservationType type, User user, Station station, Bike bike) {
        this.startInstant = startInstant;
        this.type = type;
        this.user = user;
        this.station = station;
        this.bike = bike;
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

    public void expire() {
        this.state = ReservationState.EXPIRED;
        this.endInstant = this.startInstant + VALID_TIME;
    }
    
    /**
     * Set reservation state to successful and updates reservation end instant
     * @param endInstant: it is the time instant when user removes or returns a bike with a previous bike or slot reservation, respectively
     */

    public void resolve(int endInstant) {
        this.state = ReservationState.SUCCESSFUL;
        this.endInstant = endInstant;
    }
}