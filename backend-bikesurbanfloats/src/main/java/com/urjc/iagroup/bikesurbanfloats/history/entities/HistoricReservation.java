package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

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
    private ReservationType type;

    @Expose
    private ReservationState state;

    @Expose
    private Integer user;

    @Expose
    private Integer station;

    @Expose
    private Integer bike;

    public HistoricReservation(Reservation reservation) {
        this.id = reservation.getId();
        this.startTime = reservation.getStartInstant();
        this.endTime = reservation.getEndInstant() == -1 ? null : reservation.getEndInstant();
        this.type = reservation.getType();
        this.state = reservation.getState();
        this.user = reservation.getUser().getId();
        this.station = reservation.getStation().getId();
        this.bike = reservation.getBike() == null ? null : reservation.getBike().getId();
    }

    @Override
    public int getId() {
        return id;
    }
}
