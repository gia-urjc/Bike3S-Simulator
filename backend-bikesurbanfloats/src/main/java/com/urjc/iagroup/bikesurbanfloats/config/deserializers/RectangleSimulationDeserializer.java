package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.core.RectangleSimulation;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class RectangleSimulationDeserializer implements JsonDeserializer<RectangleSimulation>{
	
	private final static String JSON_ATTR_GEOPOINT = "position";
	private final static String JSON_ATTR_LENGTHLAT= "lengthLatitude";
	private final static String JSON_ATTR_LENGTHLON = "lengthLongitude";
	
	@Override
	public RectangleSimulation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonElement jsonPostion = json.getAsJsonObject().get(JSON_ATTR_GEOPOINT).getAsJsonObject();
		GeoPoint position = gson.fromJson(jsonPostion, GeoPoint.class);
		double lengthLatitude = json.getAsJsonObject().get(JSON_ATTR_LENGTHLAT).getAsDouble();
		double lengthLongitude = json.getAsJsonObject().get(JSON_ATTR_LENGTHLON).getAsDouble();
		
		return new RectangleSimulation(position, lengthLongitude, lengthLatitude);
	}

}
