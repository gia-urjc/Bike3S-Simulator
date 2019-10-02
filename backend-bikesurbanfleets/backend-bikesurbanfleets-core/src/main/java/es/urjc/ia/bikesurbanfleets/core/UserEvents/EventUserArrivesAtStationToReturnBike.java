/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserMemory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author holger
 */
public class EventUserArrivesAtStationToReturnBike extends EventUser {

    Station station;
    private Reservation reservation;
    private boolean activereservation;
    private boolean waitingEvent=false;

    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, Reservation reservation, boolean wait) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station, reservation, user.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.station = station;
        this.reservation = reservation;
        this.activereservation = true;
        waitingEvent=wait;
    }

    public EventUserArrivesAtStationToReturnBike(int instant, User user, Station station, boolean wait) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station));
        this.newEntities = null;
        this.oldEntities=null;
        this.station = station;
        this.activereservation = false;
        this.reservation = null;
        waitingEvent=wait;
    }

    @Override
    public EventUser execute() throws Exception {
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
        //set the result of the event
        //the result of EventUserArrivesAtStationToReturnBike is either SUCCESS or FAIL
        ADDITIONAL_INFO info=null;
        if(waitingEvent) info=ADDITIONAL_INFO.RETRY_EVENT;
        if (returned) setResultInfo(Event.RESULT_TYPE.SUCCESS,info);
        else setResultInfo(Event.RESULT_TYPE.FAIL,info);

        //decide what to do afterwards
        EventUser e;
        if (returned) { //user returned the bike 
            debugEventLog("User returned Bike");
            e = manageFactAfterBikeReturn();
        } else { //was not able to get a bike
            user.getMemory().update(UserMemory.FactType.SLOTS_UNAVAILABLE, station);
            debugEventLog("User was not able to return the  bike");
            e = manageUserReturnDecision(DECISION_TYPE.AFTER_FAILED_BIKE_RETURN);
        }
        
        return e;
    }

    private EventUser manageFactAfterBikeReturn() throws Exception {
        GeoPoint point = user.getDestinationPlace();
        int arrivalTime = user.goToPointInCity(point);
        user.setState(User.STATE.WALK_TO_DESTINATION);
        debugEventLog("User walks to his destination place");
        return new EventUserArrivesAtDestinationInCity(this.instant + arrivalTime, user, point);
    }

}
