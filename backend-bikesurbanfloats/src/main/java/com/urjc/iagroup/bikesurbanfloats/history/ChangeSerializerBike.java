package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;

public class ChangeSerializerBike implements ChangeSerializer<Bike> {

	@Override
	public JsonObject getChanges(Bike oldBike, Bike newBike) {
		
		return null;
	}

}
