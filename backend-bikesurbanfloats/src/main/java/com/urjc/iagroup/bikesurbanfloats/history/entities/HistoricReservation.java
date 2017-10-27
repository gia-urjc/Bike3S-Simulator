package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.JsonAdapter;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.models.ReservationModel;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.IdReferenceAdapter;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("reservations")
public class HistoricReservation implements HistoricEntity, ReservationModel<HistoricBike, HistoricStation, HistoricUser> {

    private int id;
    private int startTime;
    private int endTime;

    private ReservationType type;
    private ReservationState state;

    @JsonAdapter(IdReferenceAdapter.class)
    private HistoricUser user;

    @JsonAdapter(IdReferenceAdapter.class)
    private HistoricStation station;

    @JsonAdapter(IdReferenceAdapter.class)
    private HistoricBike bike;

    public HistoricReservation(Reservation reservation) {
        this.id = reservation.getId();
        this.startTime = reservation.getStartInstant();
        this.endTime = reservation.getEndInstant();
        this.type = reservation.getType();
        this.state = reservation.getState();
        this.user = new HistoricUser(reservation.getUser());
        this.station = new HistoricStation(reservation.getStation());
        this.bike = reservation.getBike() == null ? null : new HistoricBike(reservation.getBike());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getStartInstant() {
        return startTime;
    }

    @Override
    public int getEndInstant() {
        return endTime;
    }

    @Override
    public ReservationType getType() {
        return type;
    }

    @Override
    public ReservationState getState() {
        return state;
    }

    @Override
    public HistoricUser getUser() {
        return user;
    }

    @Override
    public HistoricStation getStation() {
        return station;
    }

    @Override
    public HistoricBike getBike() {
        return bike;
    }
}
