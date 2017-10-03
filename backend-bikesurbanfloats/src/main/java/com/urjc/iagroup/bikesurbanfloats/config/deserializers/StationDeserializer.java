package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class StationDeserializer implements JsonDeserializer<Station>  {

	private IdGenerator bikeIdGen;
	private IdGenerator stationIdGen;
	
	public StationDeserializer(IdGenerator bikeIdGen, IdGenerator stationIdGen) {
		this.bikeIdGen = bikeIdGen;
		this.stationIdGen = stationIdGen;
	}
	
	@Override
	public Station deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonElement jsonElementBikes = json.getAsJsonObject().get("bikes");
		int capacity = json.getAsJsonObject().get("capacity").getAsInt();
		List<Bike> bikes = new ArrayList<>(Collections.nCopies(capacity, null));

		boolean isArray = jsonElementBikes.isJsonArray();
		JsonArray jsonArrayBikes = isArray ? jsonElementBikes.getAsJsonArray() : null;
		int n = isArray ? jsonArrayBikes.size() : jsonElementBikes.getAsInt();
		for (int i = 0; i < n; i++) {
			Bike bike = isArray ? gson.fromJson(jsonArrayBikes.get(i), Bike.class) : new Bike(bikeIdGen.next());
			bikes.add(bike);
		}
		
		JsonElement jsonElemGeoP = json.getAsJsonObject().get("position");
		GeoPoint position = gson.fromJson(jsonElemGeoP, GeoPoint.class);
		return new Station(stationIdGen.next(), position, capacity, bikes);
	}
	
}
