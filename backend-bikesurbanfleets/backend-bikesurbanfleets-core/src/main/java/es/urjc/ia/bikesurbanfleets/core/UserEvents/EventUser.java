package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.log.Debug;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionLeaveSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionStation;
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

    /**
     * It is the time instant when event happens.
     */
    protected int instant;
    protected List<Entity> newEntities;
    protected List<Entity> oldEntities;
    protected List<Entity> involvedEntities;
    private RESULT_TYPE result;

    static final Event.EVENT_TYPE event_type=Event.EVENT_TYPE.USER_EVENT;
    /**
     * It is the user who is involved in the event.
     */
    protected User user;

    public EventUser(int instant, User user) {
        this.instant = instant;
        this.user = user;
    }

    public final Event.EVENT_TYPE getEventType(){
        return event_type;
    }
    
    public final int getInstant() {
        return instant;
    }

    public final User getUser() {
        return user;
    }

    public final String  toString() {
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
    };
    public final void setResult(RESULT_TYPE result){
        this.result=result;
    };


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

    /**
     * User decides what to do when he is without bike and has 3 posibilities:
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
    protected final EventUser manageUserRentalDecision(UserDecision userdecision, Event.EXIT_REASON reason) throws Exception {
        if (userdecision instanceof UserDecisionLeaveSystem) {
            user.setState(User.STATE.LEAVING);
            debugEventLog("User decides to leave the system");
            return new EventUserLeavesSystem(this.getInstant(), user, reason);
        } else if (userdecision instanceof UserDecisionStation) {
            UserDecisionStation uds = (UserDecisionStation) userdecision;
            if (!uds.reserve) { //user decides to go to a station
                user.setState(User.STATE.WALK_TO_STATION);
                int arrivalTime = user.goToStation(uds.station);
                debugEventLog("User decides to go to a station without bike reservation: " + uds.station.getId());
                return new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, uds.station);
            } else { //user decides to reserve a bike at a station
                user.setState(User.STATE.TRY_BIKE_RESERVATION);
                debugEventLog("User decides to reserve a bike at a station" + uds.station.getId());
                return new EventUserTriesToReserveBike(this.getInstant(), user, uds.station);
            }
        } else {
            throw new RuntimeException("erroneous user decision");
        }
    }

    /**
     * User decides what to do when he is with a bike and has 2 posibilities: :
     * <ul>
     * <li>go to a station (without slot reservation)
     * <li>try doing a slot reservation at a station
     * </ul>
     *
     * @param userdecision: it is 
     * UserDecisionStation. it may be with reservation intent
     * or without reservation intent for a slot
     * @return a list of generated events that will occur as a consequence of
     * teh user decision: EventUserLeavesSystem,
     * EventUserArrivesAtStationToReturnBike, EventUserTriesToReserveSlot.
     */
    protected final EventUser manageUserReturnDecision(UserDecisionStation uds) throws Exception {
        if (!uds.reserve) { //user decides to go to a station
            user.setState(User.STATE.WITH_BIKE_TO_STATION);
            int arrivalTime = user.goToStation(uds.station);
            debugEventLog("User decides to go to a station without slot reservation" + uds.station.getId());
            return new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, uds.station);
        } else { //user decides to reserve a slot at a station
            user.setState(User.STATE.TRY_SLOT_RESERVATION);
            debugEventLog("User decides to reserve a slot at a station" + uds.station.getId());
            return new EventUserTriesToReserveSlot(this.getInstant(), user, uds.station);
        }
    }

}
