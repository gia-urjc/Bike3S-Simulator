package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricBike;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricStation;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricUser;

import java.util.HashMap;
import java.util.Map;

class HistoryEntry {

    private int timeInstant;
    private Map<Integer, HistoricUser> users;
    private Map<Integer, HistoricStation> stations;
    private Map<Integer, HistoricBike> bikes;

    private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

    HistoryEntry(int timeInstant) {
        this.timeInstant = timeInstant;
        this.users = new HashMap<>();
        this.stations = new HashMap<>();
        this.bikes = new HashMap<>();

        this.entityMaps = new HashMap<>();
    }

    int getTimeInstant() {
        return timeInstant;
    }

    Map<Integer, HistoricEntity> getMapFor(Class<? extends HistoricEntity> entityClass) {
        return entityMaps.get(entityClass);
    }

    void addToMapFor(Class<? extends HistoricEntity> entityClass, HistoricEntity entity) {
        if (!entityMaps.containsKey(entityClass)) {
            entityMaps.put(entityClass, new HashMap<>());
        }

        entityMaps.get(entityClass).put(entity.getId(), entity);
    }

    Map<Integer, HistoricUser> getUsers() {
        return users;
    }

    Map<Integer, HistoricStation> getStations() {
        return stations;
    }

    Map<Integer, HistoricBike> getBikes() {
        return bikes;
    }
}
