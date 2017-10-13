package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

public interface HistoricEntity<E extends Entity> extends Entity {

    static JsonObject idChange(Entity oldEntity, Entity newEntity) {
        Gson gson = new Gson();

        JsonObject id = new JsonObject();

        if (oldEntity == null && newEntity == null) return null;

        if (oldEntity == null && newEntity != null) {
            id.add("old", gson.toJsonTree("null"));
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        if (oldEntity != null && newEntity == null) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", gson.toJsonTree("null"));
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
