/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.users.UserDecisionGoToPointInCity;
import es.urjc.ia.bikesurbanfleets.users.UserDecisionStation;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author holger
 */
public class EventUserArrivesAtStationToRentBike extends EventUser {

    private Station station;
    private Reservation reservation;
    private boolean activereservation;

    public EventUserArrivesAtStationToRentBike(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user, station, reservation, reservation.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.station = station;
        this.reservation = reservation;
        this.activereservation = true;
    }

    public EventUserArrivesAtStationToRentBike(int instant, User user, Station station) {
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
        boolean bike = false;
        if (activereservation) { //if user has a reservation it will get a bike
            if (reservation.getState() != Reservation.ReservationState.ACTIVE || reservation.getType() != Reservation.ReservationType.BIKE) {
                throw new RuntimeException("invalid program flow: user should have a valid reservatyion");
            }
            bike = user.removeBikeWithReservationFrom(station, reservation, this.instant);
        } else {//try to get a bike
            bike = user.removeBikeWithoutReservationFrom(station);
        }
        //now generate the next event
        Event e;
        if (bike) { //user got a bike without an reservation
            debugEventLog("User removed Bike");
            e= manageDecisionWithBike();
        } else { //was not able to get a bike
            user.getMemory().update(UserMemory.FactType.BIKES_UNAVAILABLE);
            debugEventLog("User did not get a bike");
            UserDecision ud = user.decideAfterFailedRental();
            e= manageUserRentalDecision(ud, Event.EXIT_REASON.EXIT_AFTER_FAILED_BIKE_RENTAL);
        }
       
        //set the result of the event
        //the result of EventUserArrivesAtStationToRentBike is either SUCCESSFUL_BIKE_RENTAL or FAILED_BIKE_RENTAL
        if (bike) setResult(Event.RESULT_TYPE.SUCCESSFUL_BIKE_RENTAL);
        else setResult(Event.RESULT_TYPE.FAILED_BIKE_RENTAL);
        
        return e;
    }

    private Event manageDecisionWithBike() throws Exception {
        UserDecision ud = user.decideAfterGettingBike();
        if (ud instanceof UserDecisionGoToPointInCity) {
            UserDecisionGoToPointInCity uds = (UserDecisionGoToPointInCity) ud;
            int arrivalTime = user.goToPointInCity(uds.point);
            user.setState(User.STATE.WITH_BIKE_ON_RIDE);
            debugEventLog("User decides to take a ride");
            return new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, uds.point);
        } else if (ud instanceof UserDecisionStation) {
            return manageUserReturnDecision((UserDecisionStation) ud);
        } else {
            throw new RuntimeException("erroneous user decision");
        }
    }
}
