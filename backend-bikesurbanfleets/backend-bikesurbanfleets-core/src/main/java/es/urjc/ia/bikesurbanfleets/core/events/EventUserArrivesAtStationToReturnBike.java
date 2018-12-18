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
import java.util.List;

/**
 *
 * @author holger
 */
public class EventUserArrivesAtStationToReturnBike extends EventUser {

    private Station station;
    private Reservation reservation;
    private boolean activereservation;

    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station, reservation, user.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.station = station;
        this.reservation = reservation;
        this.activereservation = true;
    }

    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station));
        this.newEntities = null;
        this.oldEntities=null;
        this.station = station;
        this.activereservation = false;
        this.reservation = null;
    }

    @Override
    public Event execute() throws Exception {
        user.setInstant(this.instant);
        user.setPosition(station.getPosition());
        debugEventLog("At enter the event");
        boolean returned = false;
        if (activereservation) { //if user has a reservation it will get a bike
            if (reservation.getState() != Reservation.ReservationState.ACTIVE || reservation.getType() != Reservation.ReservationType.SLOT) {
                throw new RuntimeException("invalid program flow: user should have a valid slot reservation");
            }
            returned = user.returnBikeWithReservationTo(station, reservation, this.instant);
        } else {//try to get a bike
            returned = user.returnBikeWithoutReservationTo(station);
        }
        Event e;
        if (returned) { //user returned the bike 
            debugEventLog("User returned Bike");
            e = manageFactAfterBikeReturn();
        } else { //was not able to get a bike
            user.getMemory().update(UserMemory.FactType.SLOTS_UNAVAILABLE, station);
            debugEventLog("User was not able to return the  bike");
            UserDecisionStation ud = user.decideAfterFailedReturn();
            e = manageUserReturnDecision(ud);
        }
        //set the result of the event
        //the result of EventUserArrivesAtStationToReturnBike is either SUCCESSFUL_BIKE_RETURN or FAILED_BIKE_RETURN
        if (returned) setResult(Event.RESULT_TYPE.SUCCESSFUL_BIKE_RETURN);
        else setResult(Event.RESULT_TYPE.FAILED_BIKE_RETURN);
        
        return e;
    }

    private Event manageFactAfterBikeReturn() throws Exception {
        GeoPoint point = user.getDestinationPlace();
        int arrivalTime = user.goToPointInCity(point);
        user.setState(User.STATE.WALK_TO_DESTINATION);
        debugEventLog("User walks to his destination place");
        return new EventUserArrivesAtDestinationInCity(this.instant + arrivalTime, user, point);
    }

}
