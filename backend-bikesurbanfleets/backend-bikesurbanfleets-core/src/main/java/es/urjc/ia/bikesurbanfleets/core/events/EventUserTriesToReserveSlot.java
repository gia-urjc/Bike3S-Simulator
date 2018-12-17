/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserDecisionStation;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author holger
 */
public class EventUserTriesToReserveSlot extends EventUser {

    private Station station;

    public EventUserTriesToReserveSlot(int instant, User user, Station station) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station));
        this.newEntities = null;
        this.oldEntities = null;
        this.station = station;
    }

    @Override
    public Event execute() throws Exception {
        user.setInstant(this.instant);
        debugEventLog("At enter the event");

        Reservation reservation = station.getSlotReservation(user, this.instant);
        this.involvedEntities.add(reservation);
        this.newEntities = new ArrayList<>(Arrays.asList(reservation));
        Event e;
        if (reservation.getState() == Reservation.ReservationState.ACTIVE) {   // user has been able to reserve a slot
            user.addReservation(reservation);
            e= manageUserDecisionAfterSlotReservation(reservation);
        } else {  // user has notbeen able to reserve a slot
            this.oldEntities = new ArrayList<>(Arrays.asList(reservation));
            user.getMemory().update(UserMemory.FactType.SLOT_FAILED_RESERVATION,station);
            debugEventLog("User has not been able to reserve slot");
            UserDecisionStation ud = user.decideAfterFailedSlotReservation();
            e= manageUserReturnDecision(ud);
        }
        //set the result of the event
        //the result of EventUserTriesToReserveSlot is either SUCCESSFUL_SLOT_RESERVATION or FAILED_SLOT_RESERVATION
        if (reservation.getState() == Reservation.ReservationState.ACTIVE) setResult(Event.RESULT_TYPE.SUCCESSFUL_SLOT_RESERVATION);
        else setResult(Event.RESULT_TYPE.FAILED_SLOT_RESERVATION);
        
        return e;
    }

    /**
     * if the reservation has been sucessful, the user goes towards the sation
     * and there are two possibilities: EventSlotReservationTimeout or
     * EventUserArrivesAtStationToReturnBike(with reservation)
     */
    private Event manageUserDecisionAfterSlotReservation(Reservation reservation) throws Exception {
        int arrivalTime = user.goToStation(station);
        user.setState(User.STATE.WITH_BIKE_TO_STATION);
        debugEventLog("User has been able to reserve slot and goes with bike to the station");
        if (Reservation.VALID_TIME < arrivalTime) {
            GeoPoint pointTimeOut = user.reachedPointUntilTimeOut();
            return new EventSlotReservationTimeout(this.getInstant() + Reservation.VALID_TIME, user, reservation, station, pointTimeOut);
        } else {
            return new EventUserArrivesAtStationToReturnBike(this.getInstant() + arrivalTime, user, station, reservation);
        }
    }
}
