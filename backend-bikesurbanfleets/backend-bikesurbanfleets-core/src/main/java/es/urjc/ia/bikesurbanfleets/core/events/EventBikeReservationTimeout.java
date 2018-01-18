package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.entities.User;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
    private List<Entity> entities;
    private Reservation reservation;
    private GeoPoint positionTimeOut;
    
    public EventBikeReservationTimeout(int instant, User user, Reservation reservation, GeoPoint positionTimeOut) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user, reservation));
        this.reservation = reservation;
        this.positionTimeOut = positionTimeOut;
    }
    
    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(positionTimeOut);
        reservation.expire();
        user.cancelsBikeReservation(user.getDestinationStation());
        user.getMemory().update(UserMemory.FactType.BIKE_RESERVATION_TIMEOUT);

        if (user.decidesToLeaveSystemAfterTimeout(instant)) {
            user.setPosition(null);
            user.setRoute(null);
        } else if (user.decidesToDetermineOtherStationAfterTimeout()) {
            newEvents = manageBikeReservationDecisionAtOtherStation();
        } else {
            newEvents = manageBikeReservationDecisionAtSameStationAfterTimeout();
        }

        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}