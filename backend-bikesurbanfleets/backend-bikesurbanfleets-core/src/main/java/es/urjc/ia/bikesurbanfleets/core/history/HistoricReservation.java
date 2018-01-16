package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.core.entities.Bike;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation.ReservationState;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation.ReservationType;
import es.urjc.ia.bikesurbanfleets.core.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.core.history.History;
import es.urjc.ia.bikesurbanfleets.core.history.History.IdReference;
import es.urjc.ia.bikesurbanfleets.core.history.History.Timestamp;
import es.urjc.ia.bikesurbanfleets.core.history.JsonIdentifier;
import es.urjc.ia.bikesurbanfleets.core.history.entities.HistoricBike;
import es.urjc.ia.bikesurbanfleets.core.history.entities.HistoricStation;
import es.urjc.ia.bikesurbanfleets.core.history.entities.HistoricUser;

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
    private History.Timestamp startTime;

    private Timestamp endTime;

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
        this.startTime = new Timestamp(reservation.getStartInstant());
        this.endTime = reservation.getEndInstant() == -1 ? null : new Timestamp(reservation.getEndInstant());
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
