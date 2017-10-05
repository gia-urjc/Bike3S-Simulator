package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

public class ChangeSerializerBike implements ChangeSerializer{

	@Override
	public JsonObject getChanges(Entity oldEntity, Entity newEntity) {
		
		return null;
	}

}
