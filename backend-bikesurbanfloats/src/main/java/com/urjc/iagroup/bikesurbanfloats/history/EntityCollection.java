package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to save all the entity historics of the system.
 * It provides methods to save a new entity historic and to consult them.
 * @author IAgroup
 *
 */
class EntityCollection {

    private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

    EntityCollection() {
        this.entityMaps = new HashMap<>();
    }

    Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> getEntityMaps() {
        return entityMaps;
    }
    /**
     * 
     * @param entityClass It is the map key.
     * @return a map with all the instances of the given class type.
     */
    Map<Integer, HistoricEntity> getMapFor(Class<? extends HistoricEntity> entityClass) {
        return entityMaps.get(entityClass);
    }

    /**
     * It adds a new instance of entity historic to the map.
     * @param entityClass It is the key of the amp.
     * @param entity It is the instance to add to the map.
     */
    void addToMapFor(Class<? extends HistoricEntity> entityClass, HistoricEntity entity) {
        if (!entityMaps.containsKey(entityClass)) {
            entityMaps.put(entityClass, new HashMap<>());
        }

        entityMaps.get(entityClass).put(entity.getId(), entity);
    }
}
