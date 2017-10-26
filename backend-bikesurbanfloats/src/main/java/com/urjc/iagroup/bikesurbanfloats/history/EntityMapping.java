package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.entities.*;

public enum EntityMapping {
    USER(User.class, HistoricUser.class, "users"),
    STATION(Station.class, HistoricStation.class, "stations"),
    BIKE(Bike.class, HistoricBike.class, "bikes"),
    RESERVATION(Reservation.class, HistoricReservation.class, "reservations");

    private Class<? extends Entity> entityClass;
    private Class<? extends HistoricEntity> historicClass;
    private String jsonIdentifier;

    public static EntityMapping getFor(Class<? extends Entity> entityClass) {
        for (EntityMapping mapping : values()) {
            if (mapping.entityClass == entityClass) return mapping;
        }
        return null;
    }

 EntityMapping(Class<? extends Entity> entityClass, Class<? extends HistoricEntity> historicClass, String jsonIdentifier) {
        this.entityClass = entityClass;
        this.historicClass = historicClass;
        this.jsonIdentifier = jsonIdentifier;
    }

    public Class<? extends HistoricEntity> getHistoricClass() {
        return historicClass;
    }

    public String getJsonIdentifier() {
        return jsonIdentifier;
    }
}
