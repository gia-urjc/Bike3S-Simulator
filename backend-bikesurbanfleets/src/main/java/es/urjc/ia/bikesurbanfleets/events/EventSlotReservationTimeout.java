package es.urjc.ia.bikesurbanfleets.events;

import es.urjc.ia.bikesurbanfleets.entities.Entity;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.entities.users.User;

import java.util.Arrays;
import java.util.List;

public class EventSlotReservationTimeout extends EventUser {
    private List<Entity> entities;
    private Reservation reservation;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation) {
        super(instant, user);
        this.entities = Arrays.asList(user, reservation);
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents;

        user.updatePositionAfterTimeOut();
        reservation.expire();
        user.addReservation(reservation);
        user.cancelsSlotReservation(user.getDestinationStation());
        
        if (!user.decidesToDetermineOtherStationAfterTimeout()){
            newEvents = manageSlotReservationDecisionAtSameStationAfterTimeout();
        } else {
            newEvents = manageSlotReservationDecisionAtOtherStation();
        }

        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}