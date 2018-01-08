package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.core.entities.Entity;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.core.entities.Station;
import es.urjc.ia.bikesurbanfleets.core.entities.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToReturnBikeWithReservation extends EventUser {

    private List<Entity> entities;
    private Station station;
    private Reservation reservation;


    public EventUserArrivesAtStationToReturnBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
        this.entities = Arrays.asList(user, station, reservation);
        this.station = station;
        this.reservation = reservation;
    }
    
    public Station getStation() {
        return station;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        reservation.resolve(instant);
        user.addReservation(reservation);
        user.returnBikeWithReservationTo(station);
        user.setPosition(null);
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}