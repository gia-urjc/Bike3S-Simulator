package com.urjc.iagroup.bikesurbanfloats.config;

import java.lang.reflect.Type;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class StationDeserializer implements JsonDeserializer<Station>  {

	@Override
	public Station deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonElement jsonElementBikes = json.getAsJsonObject().get("bikes");
		LinkedList<Bike> bikes = new LinkedList<>();
		if(jsonElementBikes.isJsonArray()) {
			JsonArray jsonArrayBikes = jsonElementBikes.getAsJsonArray();
			for(int j = 0; j < jsonArrayBikes.size(); j++) {
				Bike bike = gson.fromJson(json, Bike.class);
				bikes.add(bike);
			}
		}
		else {
			int numBikes = jsonElementBikes.getAsInt();
			for(int j = 0; j < numBikes; j++) {
				bikes.add(new Bike());
			}
		}
		
		JsonElement jsonElemGeoP = json.getAsJsonObject().get("position");
		GeoPoint position = gson.fromJson(jsonElemGeoP, GeoPoint.class);
		int capacity = json.getAsJsonObject().get("capacity").getAsInt();
		
		Station station = new Station(position, capacity, bikes);
		return station;
	}
	
}
