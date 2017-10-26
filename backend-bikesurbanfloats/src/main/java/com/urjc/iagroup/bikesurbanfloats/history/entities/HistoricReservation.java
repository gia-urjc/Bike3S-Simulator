package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.models.ReservationModel;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

@JsonIdentifier("reservations")
public class HistoricReservation implements HistoricEntity<HistoricReservation>, ReservationModel<HistoricBike, HistoricStation, HistoricUser> {

    private int id;
    private int startTime;
    private int endTime;

    private Reservation.ReservationType type;
    private Reservation.ReservationState state;

    private HistoricUser user;
    private HistoricStation station;
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
    public Reservation.ReservationType getType() {
        return type;
    }

    @Override
    public Reservation.ReservationState getState() {
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

    @Override
    public JsonObject makeChangeEntryFrom(HistoricReservation previousSelf) {
        return null;
    }
}
