package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("reservations")
public class HistoricReservation implements HistoricEntity {

    private int id;
    private int startTime;
    private int endTime;

    private ReservationType type;
    private ReservationState state;

    private Integer user;
    private Integer station;
    private Integer bike;

    public HistoricReservation(Reservation reservation) {
        this.id = reservation.getId();
        this.startTime = reservation.getStartInstant();
        this.endTime = reservation.getEndInstant();
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
