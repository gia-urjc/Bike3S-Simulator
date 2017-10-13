package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

public interface HistoricEntity<E extends Entity> extends Entity {

    static JsonObject idChange(Entity oldEntity, Entity newEntity) {
        JsonObject id = new JsonObject();

        if (oldEntity == null && newEntity == null) return null;

        if (oldEntity == null && newEntity != null) {
            id.add("old", JsonNull.INSTANCE);
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        if (oldEntity != null && newEntity == null) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", JsonNull.INSTANCE);
            return id;
        }

        if (oldEntity.equals(newEntity)) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        return null;
    }
	
	default JsonObject getChanges(E previousSelf) {
		if (previousSelf == null) return null;

		if (previousSelf.getId() != this.getId()) {
		    String msg = "The id of previousSelf must be identical! Got: " + previousSelf.getId() + ", expected: " + this.getId();
		    throw new IllegalArgumentException(msg);
        }

		return new JsonObject();
	}
	
}
