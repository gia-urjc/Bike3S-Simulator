package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class StationDeserializer implements JsonDeserializer<Station>  {

	private static final String JSON_ATTR_BIKES = "bikes";
	private static final String JSON_ATTR_CAPACITY = "capacity";
	private static final String JSON_ATTR_POSITION = "position";
	
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
		JsonElement jsonElementBikes = json.getAsJsonObject().get(JSON_ATTR_BIKES);
		int capacity = json.getAsJsonObject().get(JSON_ATTR_CAPACITY).getAsInt();
		List<Bike> bikes = new ArrayList<>();

		boolean isArray = jsonElementBikes.isJsonArray();
		JsonArray jsonArrayBikes = isArray ? jsonElementBikes.getAsJsonArray() : null;
		int n = isArray ? jsonArrayBikes.size() : jsonElementBikes.getAsInt();
		int naux = capacity - n;
		for (int i = 0; i < n; i++) {
			Bike bike = isArray ? gson.fromJson(jsonArrayBikes.get(i), Bike.class) : new Bike(bikeIdGen.next());
			bikes.add(bike);
			SystemInfo.bikes.add(bike);
		}
		for(int i = 0; i < naux; i++) {
			bikes.add(null);
		}
		System.out.println(bikes.size());
		
		JsonElement jsonElemGeoP = json.getAsJsonObject().get(JSON_ATTR_POSITION);
		GeoPoint position = gson.fromJson(jsonElemGeoP, GeoPoint.class);
		return new Station(stationIdGen.next(), position, capacity, bikes);
	}
	
}
