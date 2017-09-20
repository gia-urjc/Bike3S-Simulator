package com.urjc.iagroup.bikesurbanfloats.config;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class ConfigInfoDeserializer implements JsonDeserializer<ConfigInfo>  {

	@Override
	public ConfigInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonObject jsonObject = json.getAsJsonObject();
		
		ArrayList<Station> stations = new ArrayList<>();
		ArrayList<EntryPoint> entryPoints = new ArrayList<>();
		
		JsonArray jsonArrayStations = jsonObject.get("stations").getAsJsonArray();
		for(int i = 0; i < jsonArrayStations.size(); i++) {
			JsonElement jsonStationElem = jsonArrayStations.get(i);
			Station station = deserializeStation(jsonStationElem);
			
			stations.add(station);
		}
		
		JsonArray jsonArrayEntryP = jsonObject.get("entryPoints").getAsJsonArray();
		for(int i = 0; i < jsonArrayEntryP.size(); i++) {
			entryPoints.add(gson.fromJson(jsonArrayEntryP.get(i), EntryPoint.class));
		}
		
		
		
		return new ConfigInfo(stations, entryPoints);
	}

	private Station deserializeStation(JsonElement jsonStationElem) {
		
		Gson gson = new Gson();
		JsonElement jsonElementBikes = jsonStationElem.getAsJsonObject().get("bikes");
		LinkedList<Bike> bikes = new LinkedList<>();
		if(jsonElementBikes.isJsonArray()) {
			JsonArray jsonArrayBikes = jsonElementBikes.getAsJsonArray();
			for(int j = 0; j < jsonArrayBikes.size(); j++) {
				Bike bike = gson.fromJson(jsonStationElem, Bike.class);
				bikes.add(bike);
			}
		}
		else {
			int numBikes = jsonElementBikes.getAsInt();
			for(int j = 0; j < numBikes; j++) {
				bikes.add(new Bike());
			}
		}
		
		JsonElement jsonElemGeoP = jsonStationElem.getAsJsonObject().get("position");
		GeoPoint position = gson.fromJson(jsonElemGeoP, GeoPoint.class);
		int capacity = jsonStationElem.getAsJsonObject().get("capacity").getAsInt();
		
		Station station = new Station(position, capacity, bikes);
		return station;
	}

}
