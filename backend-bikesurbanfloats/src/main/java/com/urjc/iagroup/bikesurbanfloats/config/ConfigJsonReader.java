package com.urjc.iagroup.bikesurbanfloats.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.urjc.iagroup.bikesurbanfloats.config.deserializers.EntryPointDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.RectangleSimulationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.StationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

public class ConfigJsonReader {
	
	private final static String JSON_ATTR_STATION = "stations";
	private final static String JSON_ATTR_ENTRYPOINTS = "entryPoints";
	private final static String JSON_ATTR_TIME_RESERVE = "reservationTime";
	private final static String JSON_ATTR_TIME_SIMULATION = "totalTimeSimulation";
	private final static String JSON_ATTR_RANDOM_SEED = "randomSeed";
	private final static String JSON_ATTR_RECTANGLE_SIMULATION = "rectangleSimulation";
	

	private String stationsFileName;
	private String entryPointsFileName;
	private String configSimulationFileName;
	
	public ConfigJsonReader(String stationsFileName, String entryPointsFileName, String configSimulationFileName) {
		this.stationsFileName = stationsFileName;
		this.entryPointsFileName = entryPointsFileName;
		this.configSimulationFileName = configSimulationFileName;
	}
	
	public void readJson() throws FileNotFoundException {
		
		IdGenerator bikeIdGen = new IdGenerator();
		IdGenerator stationIdGen = new IdGenerator();
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Station.class, new StationDeserializer(bikeIdGen, stationIdGen));
		gsonBuilder.registerTypeAdapter(EntryPoint.class, new EntryPointDeserializer());
		gsonBuilder.registerTypeAdapter(BoundingBox.class, new RectangleSimulationDeserializer());
		Gson gson = gsonBuilder.create();
		
		//Stations
		FileInputStream inputStreamJson = new FileInputStream(new File(stationsFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		SystemInfo.stations = readStations(gson, bufferedReader);
		
		//EntryPoints
		inputStreamJson = new FileInputStream(new File(entryPointsFileName));
		bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		SystemInfo.entryPoints = readEntryPoints(gson, bufferedReader);
		
		//Configuration
		inputStreamJson = new FileInputStream(new File(configSimulationFileName));
		bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		JsonObject jsonConfig = gson.fromJson(bufferedReader, JsonObject.class);
		SystemInfo.reservationTime = jsonConfig.get(JSON_ATTR_TIME_RESERVE).getAsInt();
		SystemInfo.totalTimeSimulation = jsonConfig.get(JSON_ATTR_TIME_SIMULATION).getAsInt();
		SystemInfo.randomSeed = jsonConfig.get(JSON_ATTR_RANDOM_SEED).getAsLong();
		SystemInfo.random = new RandomUtil(SystemInfo.randomSeed);
		JsonElement rectangleJson = jsonConfig.get(JSON_ATTR_RECTANGLE_SIMULATION).getAsJsonObject();
		BoundingBox rec = gson.fromJson(rectangleJson, BoundingBox.class);
		SystemInfo.rectangle = rec;
	}
	
	private ArrayList<Station> readStations(Gson gson, BufferedReader bufferedReader) {
		
		ArrayList<Station> allStations = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_STATION).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			allStations.add(gson.fromJson(elemStation, Station.class));
		}
		return allStations;
	}
	
	private ArrayList<EntryPoint> readEntryPoints(Gson gson, BufferedReader bufferedReader) {
		
		ArrayList<EntryPoint> allEntryPoints = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_ENTRYPOINTS).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			EntryPoint newEntryPoint = gson.fromJson(elemStation, EntryPoint.class);
			allEntryPoints.add(newEntryPoint);
		}
		return allEntryPoints;
	}
	

}
