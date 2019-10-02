package es.urjc.ia.bikesurbanfleets.core.UserEvents;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
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
    public EventUser execute() throws Exception {
        user.setPosition(positionTimeOut);
        debugEventLog("At enter the event");
        station.cancelSlotReservationByTimeout(reservation, instant);
        user.getMemory().update(UserMemory.FactType.SLOT_RESERVATION_TIMEOUT,station);

        //set the result of the event
        //the result of EventUserSlotReservationTimeout is always success
        setResultInfo(Event.RESULT_TYPE.SUCCESS, null);

        
        //decide what to do afterwards
        EventUser e=  manageUserReturnDecision(DECISION_TYPE.AFTER_SLOT_RESERVATION_TIMEOUT);
       
        return e;
     }
}
