package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class ChangeSerializerPerson implements ChangeSerializer<Person> {

	       

	@Override
	public JsonObject getChanges(Person oldPerson, Person newPerson) {
		if (oldPerson == null) return null;
	
		JsonObject changes = new JsonObject();
		boolean hasChanges = false;

		changes.add("id", new JsonPrimitive(newPerson.getId()));
	
		JsonObject bike = History.idChange(oldPerson.getBike(), newPerson.getBike());
	
		if (bike != null) {
			changes.add("bike", bike);
			hasChanges = true;
		}
	
		if (!newPerson.getPosition().equals(oldPerson.getPosition())) {
			double dlat = newPerson.getPosition().getLatitude() - oldPerson.getPosition().getLatitude();
			double dlon = newPerson.getPosition().getLongitude() - oldPerson.getPosition().getLongitude();
			changes.add("position", History.gson.toJsonTree(new GeoPoint(dlat, dlon)));
			hasChanges = true;
		}
	
		// TODO: implement other possible changes like destination
	
	    return hasChanges ? changes : null;
	}
}
