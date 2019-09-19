package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserDecisionStation;
import es.urjc.ia.bikesurbanfleets.worldentities.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;

public class EventUserSlotReservationTimeout extends EventUser {

    private Reservation reservation;
    private GeoPoint positionTimeOut;
    private Station station;

    public EventUserSlotReservationTimeout(int instant, User user, Reservation reservation, Station st, GeoPoint positionTimeOut) {
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
        user.setPosition(positionTimeOut);
        debugEventLog("At enter the event");
        station.cancelSlotReservationByTimeout(reservation, instant);
        user.getMemory().update(UserMemory.FactType.SLOT_RESERVATION_TIMEOUT,station);
        UserDecisionStation ud = user.decideAfterSlotReservationTimeout();
        Event e=  manageUserReturnDecision(ud);
       
        //set the result of the event
        //the result of EventUserSlotReservationTimeout is always success
        setResult(Event.RESULT_TYPE.SUCCESS);

        return e;
     }
}
