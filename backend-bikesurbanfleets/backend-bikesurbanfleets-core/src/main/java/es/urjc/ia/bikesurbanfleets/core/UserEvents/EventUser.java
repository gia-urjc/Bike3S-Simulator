package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.log.Debug;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveBike;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionReserveSlot;

import java.io.IOException;
import java.util.List;

/**
 * This class represents all events which are related to a user, i. e., posible
 * actions that a user can do at the sytem and facts derived from his actions.
 * It provides methods to manage reservation decisions and reservations
 * themselves at any event which involves a user.
 *
 * @author IAgroup
 */
public abstract class EventUser implements Event {

    public static enum EXIT_REASON {
        EXIT_AFTER_APPEARING, EXIT_AFTER_FAILED_BIKE_RESERVATION, EXIT_AFTER_FAILED_BIKE_RENTAL, EXIT_AFTER_RESERVATION_TIMEOUT,
        EXIT_AFTER_REACHING_DESTINATION
    }

    public static enum DECISION_TYPE {
        AFTER_APPEARING,
        AFTER_SUCESSFUL_BIKE_RENTAL, AFTER_FAILED_BIKE_RENTAL,
        AFTER_SUCESSFUL_BIKE_RETURN, AFTER_FAILED_BIKE_RETURN,
        AFTER_FINISHING_RIDE,
        AFTER_SUCESSFUL_BIKE_RESERVATION, AFTER_FAILED_BIKE_RESERVATION,
        AFTER_SUCESSFUL_SLOT_RESERVATION, AFTER_FAILED_SLOT_RESERVATION,
        AFTER_BIKE_RESERVATION_TIMEOUT, AFTER_SLOT_RESERVATION_TIMEOUT

    }

    /**
     * It is the time instant when event happens.
     */
    protected int instant;
    protected List<Entity> newEntities;
    protected List<Entity> oldEntities;
    protected List<Entity> involvedEntities;
    private RESULT_TYPE result;
    private ADDITIONAL_INFO additionalInfo;

    static final Event.EVENT_TYPE event_type = Event.EVENT_TYPE.USER_EVENT;
    /**
     * It is the user who is involved in the event.
     */
    protected User user;

    public EventUser(int instant, User user) {
        this.instant = instant;
        this.user = user;
    }

    public final Event.EVENT_TYPE getEventType() {
        return event_type;
    }

    public final int getInstant() {
        return instant;
    }

    public final User getUser() {
        return user;
    }

    public final String toString() {
        return print();
    }

    public final List<Entity> getNewEntities() {
        return newEntities;
    }

    public final List<Entity> getOldEntities() {
        return oldEntities;
    }

    public final List<Entity> getInvolvedEntities() {
        return involvedEntities;
    }

    public final RESULT_TYPE getResult() {
        return result;
    }

    public ADDITIONAL_INFO getAdditionalInfo() {
        return additionalInfo;
    }

    public final void setResultInfo(RESULT_TYPE result, ADDITIONAL_INFO additionalInfo) {
        this.result = result;
        this.additionalInfo = additionalInfo;
    }

    /**
     * It proccesses the event so that the relevant changes at the system occur.
     */
    public abstract EventUser execute() throws Exception;


    /*
        =================
        DEBUG METHODS
        =================
     */
    /**
     * If debug mode is activated in the configuration file, logs will be
     * activated to debug users
     *
     * @throws IOException
     */
    public final void debugEventLog(String message) {
        try {
            Debug.log(message, user, this);
        } catch (IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }

    /**
     * If debug mode is activated in the configuration file, logs will be
     * activated to debug users
     *
     * @throws IOException
     */
    public final void debugEventLog() {
        try {
            Debug.log(user, this);
        } catch (IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }

    public final void debugClose() {
        try {
            Debug.closeLog(user, user.getId());
        } catch (IOException e) {
            MessageGuiFormatter.showErrorsForGui(e);
        }
    }

    /*
        =================
        END - DEBUG METHODS
        =================

     */

 /* User decides what to do when he is without bike and has 3 posibilities:
     * go to a station (without reservation)It tries to make the bike
     * reservation:
     * <ul>
     * <li>leave system (reason is the one passed as parameter)
     * <li>go to a station (without reservation)
     * <li>try doing a reservation a t a station
     * </ul>
     *
     * @param userdecision: it is either UserDecisionLeaveSystem or
     * UserDecisionStation. in teh latter case it may be with reservation intent
     * or without reservation intent
     * @return a list of generated events that will occur as a consequence of
     * teh user decision: EventUserLeavesSystem,
     * EventUserArrivesAtStationToRentBike, EventUserTriesToReserveBike.
     */
    protected final EventUser manageUserRentalDecision(DECISION_TYPE reason) throws Exception {
        UserDecision ud;
        EXIT_REASON possibleexit;
        switch (reason) {
            case AFTER_APPEARING:
                ud = user.decideAfterAppearning();
                possibleexit = EXIT_REASON.EXIT_AFTER_APPEARING;
                break;
            case AFTER_FAILED_BIKE_RENTAL:
                ud = user.decideAfterFailedRental();
                possibleexit = EXIT_REASON.EXIT_AFTER_FAILED_BIKE_RENTAL;
                break;
            case AFTER_FAILED_BIKE_RESERVATION:
                ud = user.decideAfterFailedBikeReservation();
                possibleexit = EXIT_REASON.EXIT_AFTER_FAILED_BIKE_RESERVATION;
                break;
            case AFTER_BIKE_RESERVATION_TIMEOUT:
                ud = user.decideAfterBikeReservationTimeout();
                possibleexit = EXIT_REASON.EXIT_AFTER_RESERVATION_TIMEOUT;
                break;
            default:
                throw new RuntimeException("decision type not allowed");
        }
        if (ud instanceof UserDecisionLeaveSystem) {
            user.setState(User.STATE.LEAVING);
            debugEventLog("User decides to leave the system");
            return new EventUserLeavesSystem(this.instant, user, possibleexit);
        } else if (ud instanceof UserDecisionGoToStation) {
            UserDecisionGoToStation uds = (UserDecisionGoToStation) ud;
            //check twhether this is a waiting decision
            if (reason == DECISION_TYPE.AFTER_FAILED_BIKE_RENTAL && ((EventUserArrivesAtStationToRentBike) this).station == uds.station) {
                debugEventLog("User decides to wait at station to get bike: " + uds.station.getId());
                return new EventUserArrivesAtStationToRentBike(this.instant + GlobalConfigurationParameters.USERWAITING_INTERVAL, user, uds.station, true);
            } else {
                user.setState(User.STATE.WALK_TO_STATION);
                int arrivalTime = user.goToStation(uds.station);
                debugEventLog("User decides to go to a station without bike reservation: " + uds.station.getId());
                return new EventUserArrivesAtStationToRentBike(this.instant + arrivalTime, user, uds.station, false);
            }
        } else if (ud instanceof UserDecisionReserveBike) {
            UserDecisionReserveBike uds = (UserDecisionReserveBike) ud;
            //check twhether this is a waiting decision
            if (reason == DECISION_TYPE.AFTER_FAILED_BIKE_RESERVATION && ((EventUserTriesToReserveBike) this).station == uds.station) {
                debugEventLog("User decides to wait to try another reservation at a station" + uds.station.getId());
                return new EventUserTriesToReserveBike(this.instant + GlobalConfigurationParameters.USERWAITING_INTERVAL, user, uds.station, true);
            } else {
                user.setState(User.STATE.TRY_BIKE_RESERVATION);
                debugEventLog("User decides to reserve a bike at a station" + uds.station.getId());
                return new EventUserTriesToReserveBike(this.instant, user, uds.station, false);
            }
        } else {
            throw new RuntimeException("erroneous user decision");
        }
    }

    /**
     * User decides what to do when he just got a bike: There are two
     * possibilities:
     * <ul>
     * <li>go to a point in the city (for a ride)
     * <li>try doing a slot reservation at a station
     * </ul>
     *
     * @param userdecision: it is UserDecisionStation. it may be
     * UserDecisionGoToPointInCity or UserDecisionGoToStation (without reserving
     * a slot) or UserDecisionReserveSlot
     * @return a list of generated events that will occur as a consequence of
     * teh user decision: EventUserLeavesSystem,
     * EventUserArrivesAtStationToReturnBike, EventUserTriesToReserveSlot.
     */
    protected final EventUser manageUserDecisionAfterGettingBike(DECISION_TYPE reason) throws Exception {
        UserDecision ud;
        switch (reason) {
            case AFTER_SUCESSFUL_BIKE_RENTAL:
                ud = user.decideAfterGettingBike();
                break;
            default:
                throw new RuntimeException("decision type not allowed");
        }
        if (ud instanceof UserDecisionGoToPointInCity) {
            UserDecisionGoToPointInCity uds = (UserDecisionGoToPointInCity) ud;
            int arrivalTime = user.goToPointInCity(uds.point);
            user.setState(User.STATE.WITH_BIKE_ON_RIDE);
            debugEventLog("User decides to take a ride");
            return new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, uds.point);
        } else {
            return analyzeReturnDecision(ud, reason);
        }
    }

    /**
     * User decides what to do when he wants to return a bike: :
     * <ul>
     * <li>go to a station (without slot reservation)
     * <li>try doing a slot reservation at a station
     * </ul>
     *
     * @param userdecision: it is UserDecisionStation. it may be with
     * reservation intent or without reservation intent for a slot
     * @return a list of generated events that will occur as a consequence of
     * teh user decision: EventUserLeavesSystem,
     * EventUserArrivesAtStationToReturnBike, EventUserTriesToReserveSlot.
     */
    protected final EventUser manageUserReturnDecision(DECISION_TYPE reason) throws Exception {
        UserDecision ud;
        switch (reason) {
            case AFTER_FAILED_BIKE_RETURN:
                ud = user.decideAfterFailedReturn();
                break;
            case AFTER_FINISHING_RIDE:
                ud = user.decideAfterFinishingRide();
                break;
            case AFTER_FAILED_SLOT_RESERVATION:
                ud = user.decideAfterFailedSlotReservation();
                break;
            case AFTER_SLOT_RESERVATION_TIMEOUT:
                ud = user.decideAfterSlotReservationTimeout();
                break;
            default:
                throw new RuntimeException("decision type not allowed");
        }
        return analyzeReturnDecision(ud, reason);
    }

    private final EventUser analyzeReturnDecision(UserDecision userdecision, DECISION_TYPE reason) throws Exception {
        if (userdecision instanceof UserDecisionGoToStation) {
            UserDecisionGoToStation uds = (UserDecisionGoToStation) userdecision;
            //check waiting event
            if (reason == DECISION_TYPE.AFTER_FAILED_BIKE_RETURN && ((EventUserArrivesAtStationToReturnBike) this).station == uds.station) {
                debugEventLog("User decides to wait at station without slot reservation" + uds.station.getId());
                return new EventUserArrivesAtStationToReturnBike(this.instant + GlobalConfigurationParameters.USERWAITING_INTERVAL, user, uds.station, true);
            } else {
                user.setState(User.STATE.WITH_BIKE_TO_STATION);
                int arrivalTime = user.goToStation(uds.station);
                debugEventLog("User decides to go to a station without slot reservation" + uds.station.getId());
                return new EventUserArrivesAtStationToReturnBike(this.instant + arrivalTime, user, uds.station, false);
            }
        } else if (userdecision instanceof UserDecisionReserveSlot) {
            UserDecisionReserveSlot uds = (UserDecisionReserveSlot) userdecision;
            //check waiting event
            if (reason == DECISION_TYPE.AFTER_FAILED_SLOT_RESERVATION && ((EventUserTriesToReserveSlot) this).station == uds.station) {
                debugEventLog("User decides to wait to try another slot reservation at a station" + uds.station.getId());
                return new EventUserTriesToReserveSlot(this.instant + GlobalConfigurationParameters.USERWAITING_INTERVAL, user, uds.station, true);
            } else {
                user.setState(User.STATE.TRY_SLOT_RESERVATION);
                debugEventLog("User decides to reserve a slot at a station" + uds.station.getId());
                return new EventUserTriesToReserveSlot(this.instant, user, uds.station, false);
            }
        } else {
            throw new RuntimeException("erroneous user decision");
        }
    }
}
