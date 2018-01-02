package es.urjc.ia.bikesurbanfleets.events;

import es.urjc.ia.bikesurbanfleets.entities.Entity;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.entities.users.User;
import es.urjc.ia.bikesurbanfleets.entities.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventBikeReservationTimeout extends EventUser {
    private List<Entity> entities;
    private Reservation reservation;
    
    public EventBikeReservationTimeout(int instant, User user, Reservation reservation) {
        super(instant, user);
        this.entities = Arrays.asList(user, reservation);
        this.reservation = reservation;
    }
    
    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.updatePositionAfterTimeOut();    
        reservation.expire();
        user.addReservation(reservation);
        user.cancelsBikeReservation(user.getDestinationStation());
        user.getMemory().update(UserMemory.FactType.BIKE_RESERVATION_TIMEOUT);

        if (user.decidesToLeaveSystemAfterTimeout(instant)) {
            user.setPosition(null);
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