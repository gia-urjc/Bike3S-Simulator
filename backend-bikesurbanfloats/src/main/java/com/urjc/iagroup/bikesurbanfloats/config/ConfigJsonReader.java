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
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.BoundarySimulationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.StationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
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
	
	public SystemInfo readJson() throws FileNotFoundException {

		SystemInfo systemInfo = new SystemInfo();
		
		Gson gson = createAndConfigureGson(systemInfo);
		
		//Configuration
		readGlobalConfigurations(gson, systemInfo);

		//Stations
		readStations(gson, systemInfo);
		
		//EntryPoints
		readEntryPoints(gson, systemInfo);
		
		//RandomUtils
		systemInfo.boundingBox = new BoundingBox(systemInfo.random);
		
		return systemInfo;
	}
	
	
	private Gson createAndConfigureGson(SystemInfo systemInfo) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Station.class, new StationDeserializer(systemInfo.bikeIdGen, systemInfo.stationIdGen));
		gsonBuilder.registerTypeAdapter(EntryPoint.class, new EntryPointDeserializer());
		gsonBuilder.registerTypeAdapter(BoundingBox.class, new BoundarySimulationDeserializer());
		Gson gson = gsonBuilder.create();
		return gson;
	}

	private void readGlobalConfigurations(Gson gson, SystemInfo systemInfo) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(configSimulationFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		JsonObject jsonConfig = gson.fromJson(bufferedReader, JsonObject.class);
		systemInfo.reservationTime = jsonConfig.get(JSON_ATTR_TIME_RESERVE).getAsInt();
		systemInfo.totalTimeSimulation = jsonConfig.get(JSON_ATTR_TIME_SIMULATION).getAsInt();
		systemInfo.randomSeed = jsonConfig.get(JSON_ATTR_RANDOM_SEED).getAsLong();
		systemInfo.random = new RandomUtil(systemInfo.randomSeed);
		JsonElement rectangleJson = jsonConfig.get(JSON_ATTR_RECTANGLE_SIMULATION).getAsJsonObject();
		BoundingBox rec = gson.fromJson(rectangleJson, BoundingBox.class);
		systemInfo.boundingBox = rec;
	}
	
	private void readStations(Gson gson, SystemInfo systemInfo) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(stationsFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		
		ArrayList<Station> allStations = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_STATION).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			allStations.add(gson.fromJson(elemStation, Station.class));
		}
		systemInfo.stations = allStations;
		systemInfo.stations.stream().forEach(s -> systemInfo.bikes.addAll(s.getBikes()));
	}
	
	private void readEntryPoints(Gson gson, SystemInfo systemInfo) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(entryPointsFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		
		ArrayList<EntryPoint> allEntryPoints = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_ENTRYPOINTS).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			EntryPoint newEntryPoint = gson.fromJson(elemStation, EntryPoint.class);
			allEntryPoints.add(newEntryPoint);
		}
		systemInfo.entryPoints = allEntryPoints;
	}
	

}
