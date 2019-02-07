/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserMemory;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author holger
 */
public class EventUserTriesToReserveBike extends EventUser {

    private Station station;

    public EventUserTriesToReserveBike(int instant, User user, Station station) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station));
        this.newEntities = null;
        this.oldEntities=null;
        this.station = station;
    }

    @Override
    public Event execute() throws Exception {
        user.setInstant(this.instant);
        debugEventLog("At enter the event");

        Reservation reservation = station.getBikeReservation(user, this.instant);
        this.involvedEntities.add(reservation);
        this.newEntities = new ArrayList<>(Arrays.asList(reservation));
        Event e;
        if (reservation.getState() == Reservation.ReservationState.ACTIVE) {   // user has been able to reserve a bike
            this.involvedEntities.add(reservation.getBike());
            e = manageFactsAfterReservation(reservation);
        } else {  // user has notbeen able to reserve a bike
            this.oldEntities = new ArrayList<>(Arrays.asList(reservation));
            debugEventLog("User has not been able to reserve bike");
            UserDecision ud = user.decideAfterFailedBikeReservation();
            e = manageUserRentalDecision(ud, Event.EXIT_REASON.EXIT_AFTER_FAILED_BIKE_RENTAL);
        }
        //set the result of the event
        //the result of EventUserTriesToReserveBike is either SUCCESSFUL_BIKE_RESERVATION or FAILED_BIKE_RESERVATION
        if (reservation.getState() == Reservation.ReservationState.ACTIVE) setResult(Event.RESULT_TYPE.SUCCESSFUL_BIKE_RESERVATION);
        else setResult(Event.RESULT_TYPE.FAILED_BIKE_RESERVATION);
        
        return e;
    }

    /**
     * if the reservation has been sucessful, the user goes towards the sation
     * and there are two possibilities: EventBikeReservationTimeout or
     * EventUserArrivesAtStationToRentBike(with reservation)
     */
    private Event manageFactsAfterReservation(Reservation reservation) throws Exception {
        int arrivalTime = user.goToStation(station);
        user.setState(User.STATE.WALK_TO_STATION);
        debugEventLog("User has been able to reserve bike and walks to the station");
        if (Reservation.VALID_TIME < arrivalTime) {
            GeoPoint pointTimeOut = user.reachedPointUntilTimeOut();
            return new EventBikeReservationTimeout(this.getInstant() + Reservation.VALID_TIME, user, reservation, station, pointTimeOut);
        } else {
            return new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, station, reservation);
        }
    }

}
