package com.urjc.iagroup.bikesurbanfloats.entities.models;

import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;

public interface ReservationModel<U extends UserModel, B extends BikeModel, S extends StationModel<B>> {

    int getStartInstant();
    int getEndInstant();

    Reservation.ReservationType getType();
    Reservation.ReservationState getState();

    U getUser();
    S getStation();
    B getBike();
}
