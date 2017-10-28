package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

class EntityCollection {

    private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

    EntityCollection() {
        this.entityMaps = new HashMap<>();
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
