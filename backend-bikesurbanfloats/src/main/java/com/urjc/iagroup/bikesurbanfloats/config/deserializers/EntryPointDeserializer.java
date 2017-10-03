package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.EntryPointFactory;
import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class EntryPointDeserializer implements JsonDeserializer<EntryPoint>  {
	private final static String JSON_ATTR_DISTRIBUTION = "distribution";
	private final static String JSON_ATTR_DISTRIBUTIONTYPE = "distributionType";
	private EntryPointFactory entryPointFactory;
	
	public EntryPointDeserializer() {
		this.entryPointFactory = new EntryPointFactory();
	}
	
	@Override
	public EntryPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonObject	jsonElementEntryP = json.getAsJsonObject();
		DistributionType distributionType = null;
		
		// if entryPoint does'nt contain a distribution attribute, it's of type single (one person)
		if (jsonElementEntryP.has(JSON_ATTR_DISTRIBUTION)) {
			String distributionStr = jsonElementEntryP.get(JSON_ATTR_DISTRIBUTION)
					.getAsJsonObject().get(JSON_ATTR_DISTRIBUTIONTYPE).getAsString();
			distributionType = DistributionType.valueOf(distributionStr);
		}
		else {
			distributionType = DistributionType.NONEDISTRIBUTION;		
			}
		
		return entryPointFactory.createEntryPoint(jsonElementEntryP, distributionType);
		
	}

}
