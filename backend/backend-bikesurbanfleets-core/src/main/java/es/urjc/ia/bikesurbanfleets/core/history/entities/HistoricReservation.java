package es.urjc.ia.bikesurbanfleets.core.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.core.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.core.history.History;
import es.urjc.ia.bikesurbanfleets.core.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.core.entities.Bike;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation;

/**
 * It contains the relevant information of a specific reservation, e. g, its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("reservations")
public class HistoricReservation implements HistoricEntity {

    @Expose
    private int id;

    @Expose
    private int startTime;

    private Integer endTime;

    @Expose
    private Reservation.ReservationType type;

    @Expose
    private Reservation.ReservationState state;

    @Expose
    private History.IdReference user;

    @Expose
    private History.IdReference station;

    @Expose
    private History.IdReference bike;

    public HistoricReservation(Reservation reservation) {
        Bike bike = reservation.getBike();
        this.id = reservation.getId();
        this.startTime = reservation.getStartInstant();
        this.endTime = reservation.getEndInstant() == -1 ? null : reservation.getEndInstant();
        this.type = reservation.getType();
        this.state = reservation.getState();
        this.user = new History.IdReference(HistoricUser.class, reservation.getUser().getId());
        this.station = new History.IdReference(HistoricStation.class, reservation.getStation().getId());
        this.bike = bike == null ? null : new History.IdReference(HistoricBike.class, bike.getId());
    }

    @Override
    public int getId() {
        return id;
    }
}
