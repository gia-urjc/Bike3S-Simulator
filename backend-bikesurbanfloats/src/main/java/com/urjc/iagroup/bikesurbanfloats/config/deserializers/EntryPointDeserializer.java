package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointFactory;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.distributions.Distribution.DistributionType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to obtain the entry points in the system's own format.    
 * @author IAgroup
 *
 */
public class EntryPointDeserializer implements JsonDeserializer<List<EntryPoint>>  {

	private final static String JSON_ATTR_DISTRIBUTION = "distribution";
	private final static String JSON_ATTR_DISTR_TYPE = "type";
	
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

            // if entryPoint doesn't contain a distribution attribute, it's of single type (one user)
            if (jsonEntryPoint.has(JSON_ATTR_DISTRIBUTION)) {
                String distributionStr = jsonEntryPoint.get(JSON_ATTR_DISTRIBUTION)
                        .getAsJsonObject().get(JSON_ATTR_DISTR_TYPE).getAsString();
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
