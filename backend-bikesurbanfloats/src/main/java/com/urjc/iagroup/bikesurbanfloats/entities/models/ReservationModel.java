package com.urjc.iagroup.bikesurbanfloats.entities.models;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;

public interface ReservationModel<B extends BikeModel, S extends StationModel<B>, U extends UserModel<B, S>> {

    int getStartInstant();
    int getEndInstant();

    Reservation.ReservationType getType();
    Reservation.ReservationState getState();

    U getUser();
    S getStation();
    B getBike();
}
