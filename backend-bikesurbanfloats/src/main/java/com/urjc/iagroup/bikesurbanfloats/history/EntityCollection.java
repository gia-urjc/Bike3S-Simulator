package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to save the histories of all the entities of the system.
 * It provides methods to save a new entity history and to consult one.
 * @author IAgroup
 *
 */
class EntityCollection {
    /**
     * It is a map whose key is the class of the historic entity and whose value is another map 
     * with the entity identifier as the key and the history of the entity as the value.
     */
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
