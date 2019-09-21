package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses.IdReference;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation.ReservationState;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Reservation.ReservationType;
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

    @Expose
    private int endTime;

    @Expose
    private ReservationType type;

    @Expose
    private ReservationState state;

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
        this.endTime = reservation.getEndInstant();
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