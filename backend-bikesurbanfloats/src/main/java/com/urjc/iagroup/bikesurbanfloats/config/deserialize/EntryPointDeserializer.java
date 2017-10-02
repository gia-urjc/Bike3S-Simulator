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
import com.urjc.iagroup.bikesurbanfloats.entities.factory.EntryPointFactory;
import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class EntryPointDeserializer implements JsonDeserializer<EntryPoint>  {

	private final static String JSON_ATR_DISTRIBUTION = "distribution";
	
	private EntryPointFactory entryPointFactory;
	
	public EntryPointDeserializer() {
		this.entryPointFactory = new EntryPointFactory();
	}
	
	@Override
	public EntryPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		JsonObject jsonElementEntryP = json.getAsJsonObject();
		String distributionStr = jsonElementEntryP.get(JSON_ATR_DISTRIBUTION).getAsString();
		DistributionType distribution = DistributionType.valueOf(distributionStr);
		return entryPointFactory.createEntryPoint(jsonElementEntryP, distribution);
		
	}

}
