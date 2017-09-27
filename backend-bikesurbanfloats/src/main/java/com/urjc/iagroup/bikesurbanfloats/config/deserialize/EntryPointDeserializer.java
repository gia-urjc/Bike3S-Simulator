package com.urjc.iagroup.bikesurbanfloats.config.deserialize;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.util.Distribution;

public class EntryPointDeserializer implements JsonDeserializer<EntryPoint>  {

	@Override
	public EntryPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonObject jsonElementEntryP = json.getAsJsonObject();
		JsonElement jsonElementDistr = jsonElementEntryP.get(Distribution.POISSON.toString());
		Distribution distribution = gson.fromJson(jsonElementDistr, Distribution.class);
		switch(distribution) {
			case POISSON: return gson.fromJson(jsonElementEntryP, EntryPointPoisson.class);
			default: throw new JsonParseException("Type of EntryPoint doesn't exists");
		}
		
	}

}
