package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation.ReservationType;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
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
    public abstract List<Event> execute();


    /*
        =================
        DEBUG METHODS
        =================
    */

    /**
     * If debug mode is activated in the configuration file, logs will be activated to debug users
     * @throws IOException
     */
    public void debugEventLog(String message) {
        try {
            Debug.log(message, user, this);
        }
        catch(IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }

    /**
     * If debug mode is activated in the configuration file, logs will be activated to debug users
     * @throws IOException
     */
    public void debugEventLog() {
        try {
            Debug.log(user, this);
        }
        catch (IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }

    public void debugClose(User user, int id) {
        try {
            Debug.closeLog(user, id);
        } catch (IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }



    /*
        =================
        END - DEBUG METHODS
        =================
    */

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
    private List<Event> manageBikeReservation(Station destination) throws Exception {
        List<Event> newEvents = new ArrayList<>();

        Bike bike = user.reservesBike(destination);
        if (bike != null) {  // user has been able to reserve a bike
            Reservation reservation = new Reservation(instant, ReservationType.BIKE, user, destination, bike);
            user.addReservation(reservation);
            destination.getReservations().add(reservation);
            int arrivalTime = user.goToStation(destination);
            debugEventLog("User has been able to reserve bike. Reservation Info: " + reservation.toString());
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
            debugEventLog("User has not been able to reserve bike");
            if (user.decidesToLeaveSystemAffterFailedReservation()) {
                debugEventLog("User decides to leave the system");
                debugClose(user, user.getId());
                user.setState(User.STATE.EXIT_AFTER_FAILED_RESERVATION);
                newEvents.add(new EventUserLeavesSystem(this.getInstant(), user));
            } else if (user.decidesToDetermineOtherStationAfterFailedReservation()) {
                debugEventLog("User decides to determine other station to manage bike reservation");
                newEvents = manageBikeReservationDecisionAtOtherStation();
            } else {  // user walks to the initially chosen station
                debugEventLog("User decides to go to the initially chosen station without bike reservation");
                int arrivalTime = user.goToStation(destination);
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
    protected List<Event> manageBikeReservationDecisionAtSameStationAfterTimeout() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.getDestinationStation();
        
        if(Debug.DEBUG_MODE) {
            System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());
        }
        if (user.decidesToReserveBikeAtSameStationAfterTimeout()) {
            debugEventLog("User decides to manage bike reservation at the same station");
            newEvents = manageBikeReservation(destination);
        } else {   // user decides not to reserve
            debugEventLog("User decides to go to the initially chosen station without reservation");
            int arrivalTime = user.goToStation(destination);
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
    protected List<Event> manageBikeReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToRentBike();

        if (destination != null) {

            if(Debug.DEBUG_MODE) {
                System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());
            }
            if (user.decidesToReserveBikeAtNewDecidedStation()) {
                debugEventLog("User decides to reserve bike at new decided station");
                newEvents = manageBikeReservation(destination);
            } else {   // user decides not to reserve
                int arrivalTime = user.goToStation(destination);
                debugEventLog("User decides to go directly to the new decided station without bike reservation");
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
    private List<Event> manageSlotReservation(Station destination) throws Exception {
        List<Event> newEvents = new ArrayList<>();
        if (user.reservesSlot(destination)) {  // User has been able to reserve
            Reservation reservation = new Reservation(instant, ReservationType.SLOT, user, destination, user.getBike());
            user.addReservation(reservation);
            destination.getReservations().add(reservation);
            int arrivalTime = user.goToStation(destination);
            debugEventLog("User has been able to reserve a slot");
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
            user.getMemory().update(UserMemory.FactType.SLOT_FAILED_RESERVATION);
            debugEventLog("User has not been able to reserve a slot");
            
            if (!user.decidesToDetermineOtherStationAfterFailedReservation()) {  // user waljs to the initially chosen station
                debugEventLog("User decides to go to the initially chosen station without slot reservation");
                int arrivalTime = user.goToStation(destination);
                newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
            } else {
                debugEventLog("User decides to determine other station to manage slot reservation");
                newEvents = manageSlotReservationDecisionAtOtherStation();
            }
        }
        this.getEntities().add(destination);
        return newEvents;
    }
         
        
    protected List<Event> manageSlotReservationDecisionAtSameStationAfterTimeout() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.getDestinationStation();
        if(Debug.DEBUG_MODE) {
            System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());
        }
        if (user.decidesToReserveSlotAtSameStationAfterTimeout()) {
            debugEventLog("User decides to manage slot reservation at the same station");
            newEvents = manageSlotReservation(destination);
        } else {   // user decides not to reserve
            debugEventLog("User decides to go to the initially chosen station without slot reservation");
            int arrivalTime = user.goToStation(destination);
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }

    protected List<Event> manageSlotReservationDecisionAtOtherStation() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        Station destination = user.determineStationToReturnBike();
        
        if(Debug.DEBUG_MODE) {
            System.out.println("Destination before user arrival: " + destination.toString() + " " + user.toString());
        }

        if (user.decidesToReserveSlotAtNewDecidedStation()) {
            debugEventLog("User decides to reserve slot at new decided station");
            newEvents = manageSlotReservation(destination);
        } else {   // user decides not to reserve bike
            debugEventLog("User decides to go directly to the new decided station without slot reservation");
            int arrivalTime = user.goToStation(destination);
            newEvents.add(new EventUserArrivesAtStationToReturnBikeWithoutReservation(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;
    }
    
    protected void exceptionTreatment(Exception e) {
        MessageGuiFormatter.showErrorsForGui(e);
        System.out.println(user.toString());
        user.leaveSystem();
        debugEventLog("Exception occurred");
        debugEventLog(ExceptionUtils.getStackTrace(e));
        debugClose(user, user.getId());
    }

}