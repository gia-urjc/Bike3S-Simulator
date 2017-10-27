package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import javax.validation.constraints.NotNull;

public interface HistoricEntity extends Entity {

    static JsonObject propertyChange(@NotNull Object oldProperty, @NotNull Object newProperty) {
        JsonObject property = new JsonObject();

        if (!oldProperty.equals(newProperty)) {
            property.add("old", History.gson.toJsonTree(oldProperty));
            property.add("new", History.gson.toJsonTree(newProperty));
        }

        return property;
    }

    static JsonObject idReferenceChange(Entity oldEntity, Entity newEntity) {
        Object oldId = oldEntity == null ? JsonNull.INSTANCE : oldEntity.getId();
        Object newId = newEntity == null ? JsonNull.INSTANCE : newEntity.getId();
        return propertyChange(oldId, newId);
    }
	
}
