/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserMemory;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author holger
 */
public class EventUserArrivesAtStationToRentBike extends EventUser {

    Station station;
    private Reservation reservation;
    private boolean activereservation;
    private boolean waitingEvent=false;

    public EventUserArrivesAtStationToRentBike(int instant, User user, Station station, Reservation reservation, boolean wait) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station, reservation, reservation.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.station = station;
        this.reservation = reservation;
        this.activereservation = true;
        waitingEvent=wait;
    }

    public EventUserArrivesAtStationToRentBike(int instant, User user, Station station, boolean wait) {
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
        double lastwalkeddist=user.getRoute().getTotalDistance(); 
        user.getMemory().addWalkedToTakeBikeDistance(lastwalkeddist);
        user.setPosition(station.getPosition());
        debugEventLog("At enter the event");
        boolean bike = false;
        if (activereservation) { //if user has a reservation it will get a bike
            if (reservation.getState() != Reservation.ReservationState.ACTIVE || reservation.getType() != Reservation.ReservationType.BIKE) {
                throw new RuntimeException("invalid program flow: user should have a valid reservatyion");
            }
            bike = user.removeBikeWithReservationFrom(station, reservation, this.instant);
        } else {//try to get a bike
            bike = user.removeBikeWithoutReservationFrom(station);
        }
        //set the result of the event
        //the result of EventUserArrivesAtStationToRentBike is either SUCCESS or FAIL
        ADDITIONAL_INFO info=null;
        if(waitingEvent) info=ADDITIONAL_INFO.RETRY_EVENT;
        if (bike) setResultInfo(Event.RESULT_TYPE.SUCCESS, info);
        else setResultInfo(Event.RESULT_TYPE.FAIL, info);

        //decide what to do afterwards
        EventUser e;
        if (bike) { //user got a bike without an reservation
            debugEventLog("User removed Bike");
            e= manageUserDecisionAfterGettingBike(DECISION_TYPE.AFTER_SUCESSFUL_BIKE_RENTAL);
        } else { //was not able to get a bike
            user.getMemory().update(UserMemory.FactType.BIKES_UNAVAILABLE,station);
            debugEventLog("User did not get a bike");
            e= manageUserRentalDecision(DECISION_TYPE.AFTER_FAILED_BIKE_RENTAL);
        }
      
        return e;
    }
}