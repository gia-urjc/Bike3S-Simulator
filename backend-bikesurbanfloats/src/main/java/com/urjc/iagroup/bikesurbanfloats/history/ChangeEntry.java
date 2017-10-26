package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

class ChangeEntry {

    private int timeInstant;
    private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

    ChangeEntry(int timeInstant) {
        this.timeInstant = timeInstant;
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
}
