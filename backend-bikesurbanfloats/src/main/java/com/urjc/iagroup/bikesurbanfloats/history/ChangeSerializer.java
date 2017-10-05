package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

public interface ChangeSerializer<E extends Entity> {
	
	JsonObject getChanges(E oldEntity, E newEntity);
	
}
