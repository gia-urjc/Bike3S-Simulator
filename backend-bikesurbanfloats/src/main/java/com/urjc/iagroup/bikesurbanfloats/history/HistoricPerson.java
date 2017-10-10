package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class HistoricPerson extends Person  {

	private HistoricBike bike;

    HistoricPerson(Person user) {
        super(user.getId(), new GeoPoint(user.getPosition()));

        this.bike = user.getBike() == null ? null : new HistoricBike(user.getBike());
    }

    // only if there are changes, the method registry them
    JsonObject getChanges(HistoricPerson previousSelf) {
    	Gson gson = new Gson();
    	if (previousSelf == null) return null;

        JsonObject changes = new JsonObject();
        JsonObject oldEntity = new JsonObject(); 
        JsonObject newEntity = new JsonObject(); 
        boolean hasChanges = false;

        oldEntity.add("id", new JsonPrimitive(this.getId()));
        newEntity.add("id", new JsonPrimitive(this.getId()));

        JsonObject[] bike = History.idChange(previousSelf.bike, this.bike);

        if (bike != null) {
            old.add("bike", bike[0]);
            new.add("bike", bike[1]);
            hasChanges = true;
        }

        if (!this.getPosition().equals(previousSelf.getPosition())) {
            old.add("position", gson.toJsonTree(previousSelf.getPosition()));
            new.add("position", gson.toJsonTree(this.getPosition()));
            hasChanges = true;
        }

        // TODO: implement other possible changes like destination
        
        changes.add("old", old);
        changes.add("new", new);

        return hasChanges ? changes : null;
    }
}
