package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointFactory;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.distributions.Distribution.DistributionType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EntryPointDeserializer implements JsonDeserializer<List<EntryPoint>>  {

	private final static String JSON_ATR_DISTRIBUTION = "distribution";
	private final static String JSON_ATR_DISTR_TYPE = "type";
	
	private EntryPointFactory entryPointFactory;
	
	public EntryPointDeserializer() {
		this.entryPointFactory = new EntryPointFactory();
	}
	
	@Override
	public List<EntryPoint> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		List<EntryPoint> entryPoints = new ArrayList<>();

        for (JsonElement element : json.getAsJsonArray()) {
            JsonObject jsonEntryPoint = element.getAsJsonObject();
            DistributionType distributionType;

            // if entryPoint does'nt contain a distribution attribute, it's of type single (one user)
            if (jsonEntryPoint.has(JSON_ATR_DISTRIBUTION)) {
                String distributionStr = jsonEntryPoint.get(JSON_ATR_DISTRIBUTION)
                        .getAsJsonObject().get(JSON_ATR_DISTR_TYPE).getAsString();
                distributionType = DistributionType.valueOf(distributionStr);
            }
            else {
                distributionType = DistributionType.NONEDISTRIBUTION;
            }

            entryPoints.add(entryPointFactory.createEntryPoint(jsonEntryPoint, distributionType));
        }
		
		return entryPoints;
		
	}

}
