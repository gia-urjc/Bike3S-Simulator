package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserMemory;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents all events which are related to a user, i. e., posible
 * actions that a user can do at the sytem and facts derived from his actions.
 * It provides methods to manage reservation decisions and reservations themselves
 * at any event which involves a user.
 *
 * @author IAgroup
 */
public abstract class EventUser implements Event {
    /**
     * It is the time instant when event happens.
     */
    protected int instant;

    /**
     * It is the user who is involved in the event.
     */
    protected User user;

    public EventUser(int instant, User user) {
        this.instant = instant;
        this.user = user;
    }

    public int getInstant() {
        return instant;
    }

    public User getUser() {
        return user;
    }

    public String toString() {
        return print();
    }

    /**
     * It proccesses the event so that the relevant changes at the system occur.
     */
    public abstract List<Event> execute() throws Exception;

    /**
     * It tries to make the bike reservation:
     * <ul>
     * <li>If it is possible, user may have time to reach the station  while reservation is active or may not.
     * <li>If it isn't possible, in case of the user decides not to leave the system,
     * he makes a decision: to arrive at chosen station without reservation or
     * to repeat all the process after deciding to reserve at a new chosen station.
     * </ul>
     *
     * @param destination: it is the station for which user wants to make a bike reservation.
     *                     This parameter can be the previous chosen station or a new decided destination station.
     * @return a list of generated events that will occur as a consequence of trying to reserve a bike.
     */
    public List<Event> manageBikeReservation(Station destination) throws Exception {
        List<Event> newEvents = new ArrayList<>();

        int arrivalTime = user.timeToReach();
        Bike bike = user.reservesBike(destination);
        if (bike != null) {  // user has been able to reserve a bike  
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination, bike);
            user.addReservation(reservation);
            destination.getReservations().add(reservation);
            if (Reservation.VALID_TIME < arrivalTime) {
                GeoPoint pointTimeOut = user.reachedPointUntilTimeOut();
                newEvents.add(new EventBikeReservationTimeout(this.getInstant() + Reservation.VALID_TIME, user, reservation, pointTimeOut));
            } else {
                newEvents.add(new EventUserArrivesAtStationToRentBikeWithReservation(this.getInstant() + arrivalTime, user, destination, reservation));
            }
        } else {  // user hasn't been able to reserve a bike
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination);
            destination.getReservations().add(reservation);
            user.addReservation(reservation);
            user.getMemory().update(UserMemory.FactType.BIKE_FAILED_RESERVATION);
            if (user.decidesToLeaveSystemAffterFailedReservation(instant)) {
                user.setPosition(null);
            } else if (user.decidesToDetermineOtherStationAfterFailedReservation()) {
                newEvents = manageBikeReservationDecisionAtOtherStation();
            } else {  // user walks to the initially chosen station
                newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
            }
        }

        this.getEntities().add(destination);

        return newEvents;
    }

    /**
     * It is a recursive method.
     * At this method, user decides if he'll try to make again the bike reservation at previous chosen station.
     *
     * @return a list of generated events that will ocurr as a consequence of making the decision.
     */
    public List<Event> manageBikeReservationDecisionAtSameStationAfterTimeout() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.getDestinationStation();
        user.setDestination(destination);
        int arrivalTime = user.timeToReach();
        System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());

        if (user.decidesToReserveBikeAtSameStationAfterTimeout()) {
            newEvents = manageBikeReservation(destination);
        } else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }

    /**
     * This is a recursive method.
     * At this method, user decides if he'll try to make a bike reservation at a new chosen station.
     *
     * @return a list of generated events as a consequence of making the decision.
     * @throws Exception
     */
    public List<Event> manageBikeReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToRentBike(instant);

        if (destination != null) {
            user.setDestination(destination);

            int arrivalTime = user.timeToReach();
            System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());

            if (user.decidesToReserveBikeAtNewDecidedStation()) {
                newEvents = manageBikeReservation(destination);
            } else {   // user decides not to reserve
                newEvents.add(new EventUserArrivesAtStationToRentBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
            }
        }
        return newEvents;
    }

    /**
     * @param destination
     * @return
     * @throws Exception
     */
    public List<Event> manageSlotReservation(Station destination) throws Exception {
        List<Event> newEvents = new ArrayList<>();
        int arrivalTime = user.timeToReach();
        if (user.reservesSlot(destination)) {  // User has been able to reserve
            Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination, user.getBike());
            user.addReservation(reservation);
            destination.getReservations().add(reservation);
            if (Reservation.VALID_TIME < arrivalTime) {
                GeoPoint pointTimeOut = user.reachedPointUntilTimeOut();
                newEvents.add(new EventSlotReservationTimeout(this.getInstant() + Reservation.VALID_TIME, user, reservation, pointTimeOut));
            } else {
                newEvents.add(new EventUserArrivesAtStationToReturnBikeWithReservation(this.getInstant() + arrivalTime, user, destination, reservation));
            }
        } else {  // user hasn't been able to reserve a slot
            Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination);
            destination.getReservations().add(reservation);
            user.addReservation(reservation);
            //aq:
            if (!user.decidesToDetermineOtherStationAfterFailedReservation()) {  // user waljs to the initially chosen station
                newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
            } else {
                newEvents = manageSlotReservationDecisionAtOtherStation();
            }
        }
        this.getEntities().add(destination);
        return newEvents;
    }

    public List<Event> manageSlotReservationDecisionAtSameStationAfterTimeout() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.getDestinationStation();
        user.setDestination(destination);
        int arrivalTime = user.timeToReach();
        System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());

        if (user.decidesToReserveSlotAtSameStationAfterTimeout()) {
            newEvents = manageSlotReservation(destination);
        } else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }

    public List<Event> manageSlotReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToReturnBike(instant);
        user.setDestination(destination);

        int arrivalTime = user.timeToReach();
        System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());

        if (user.decidesToReserveSlotAtNewDecidedStation()) {
            newEvents = manageSlotReservation(destination);
        } else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }

}