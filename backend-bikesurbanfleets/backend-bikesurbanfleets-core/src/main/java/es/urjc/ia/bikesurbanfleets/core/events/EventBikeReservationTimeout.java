package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserDecision;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;

public class EventBikeReservationTimeout extends EventUser {

    private Reservation reservation;
    private GeoPoint positionTimeOut;
    private Station station;

    public EventBikeReservationTimeout(int instant, User user, Reservation reservation, Station st, GeoPoint positionTimeOut) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user,st, reservation, reservation.getBike()));
        this.newEntities = null;
        this.oldEntities=new ArrayList<>(Arrays.asList(reservation));
        this.reservation = reservation;
        this.positionTimeOut = positionTimeOut;
        this.station = st;
    }

    @Override
    public Event execute() throws Exception {
        user.setInstant(this.instant);
        user.setPosition(positionTimeOut);
        debugEventLog("At enter the event");
        station.cancelBikeReservationByTimeout(reservation, instant);
        user.cancelBikeReservationByTimeout(reservation);
        user.getMemory().update(UserMemory.FactType.BIKE_RESERVATION_TIMEOUT);
        UserDecision ud = user.decideAfterBikeReservationTimeout();
        Event e= manageUserRentalDecision(ud, Event.EXIT_REASON.EXIT_AFTER_RESERVATION_TIMEOUT);
       
        //set the result of the event
        //the result of EventBikeReservationTimeout is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
    }
}
