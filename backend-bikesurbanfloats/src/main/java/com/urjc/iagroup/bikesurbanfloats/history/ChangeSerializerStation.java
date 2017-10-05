package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class ChangeSerializerStation implements ChangeSerializer{

	@Override
	public JsonObject getChanges(Entity oldEntity, Entity newEntity) {
		Station oldStation = (Station) oldEntity;
		Station newStation = (Station) newEntity;
		if (oldStation == null) return null;

        JsonObject changes = new JsonObject();
        boolean hasChanges = false;

        changes.add("id", new JsonPrimitive(newStation.getId()));

        JsonObject bikes = new JsonObject();

        for (int i = 0; i < newStation.getCapacity(); i++) {
            JsonObject bike = History.idChange(oldStation.getBikes().get(i), newStation.getBikes().get(i));
            if (bike != null) {
                bikes.add(Integer.toString(i), bike);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            changes.add("bikes", bikes);
        }

        // TODO: add other possible changes

        return hasChanges ? changes : null;
	}

}
