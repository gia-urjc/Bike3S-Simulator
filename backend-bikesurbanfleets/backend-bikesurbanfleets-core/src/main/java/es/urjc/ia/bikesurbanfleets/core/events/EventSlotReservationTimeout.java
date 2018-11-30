package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserDecisionStation;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;

public class EventSlotReservationTimeout extends EventUser {

    private Reservation reservation;
    private GeoPoint positionTimeOut;
    private Station station;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, Station st, GeoPoint positionTimeOut) {
        super(instant, user);
        this.involvedEntities = new ArrayList<>(Arrays.asList(user,st, reservation));
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
        station.cancelSlotReservationByTimeout(reservation, instant);
        user.cancelSlotReservationByTimeout(reservation);
        user.getMemory().update(UserMemory.FactType.SLOT_RESERVATION_TIMEOUT);
        UserDecisionStation ud = user.decideAfterSlotReservationTimeout();
        Event e=  manageUserReturnDecision(ud);
       
        //set the result of the event
        //the result of EventSlotReservationTimeout is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
     }
}
