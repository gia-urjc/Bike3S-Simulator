package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.entities.Bike;
import es.urjc.ia.bikesurbanfleets.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.History.IdReference;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;

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
    private IdReference user;

    @Expose
    private IdReference station;

    @Expose
    private IdReference bike;

    public HistoricReservation(Reservation reservation) {
        Bike bike = reservation.getBike();
        this.id = reservation.getId();
        this.startTime = reservation.getStartInstant();
        this.endTime = reservation.getEndInstant() == -1 ? null : reservation.getEndInstant();
        this.type = reservation.getType();
        this.state = reservation.getState();
        this.user = new IdReference(HistoricUser.class, reservation.getUser().getId());
        this.station = new IdReference(HistoricStation.class, reservation.getStation().getId());
        this.bike = bike == null ? null : new IdReference(HistoricBike.class, bike.getId());
    }

    @Override
    public int getId() {
        return id;
    }
}
