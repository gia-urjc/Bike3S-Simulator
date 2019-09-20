package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;

public class EventUserBikeReservationTimeout extends EventUser {

    private Reservation reservation;
    private GeoPoint positionTimeOut;
    private Station station;
    private double distwalked;

    public EventUserBikeReservationTimeout(int instant, User user, Reservation reservation, Station st, GeoPoint positionTimeOut, double distwalkedtilTimeout) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user,st, reservation, reservation.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.reservation = reservation;
        this.positionTimeOut = positionTimeOut;
        this.station = st;
        this.distwalked=distwalkedtilTimeout;
    }

    @Override
    public EventUser execute() throws Exception {
        user.getMemory().addWalkedToTakeBikeDistance(distwalked);
        user.setPosition(positionTimeOut);
        debugEventLog("At enter the event");
        station.cancelBikeReservationByTimeout(reservation, instant);
        user.getMemory().update(UserMemory.FactType.BIKE_RESERVATION_TIMEOUT, station);
        UserDecision ud = user.decideAfterBikeReservationTimeout();
        EventUser e = manageUserRentalDecision(ud, Event.EXIT_REASON.EXIT_AFTER_RESERVATION_TIMEOUT);
       
        //set the result of the event
        //the result of EventUserBikeReservationTimeout is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
    }
}
