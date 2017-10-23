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
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.StationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

public class ConfigJsonReader {
	
	private final static String JSON_ATTR_STATION = "stations";
	private final static String JSON_ATTR_ENTRYPOINTS = "entryPoints";
	private final static String JSON_ATTR_TIME_RESERVE = "reservationTime";
	private final static String JSON_ATTR_TIME_SIMULATION = "totalTimeSimulation";
	private final static String JSON_ATTR_RANDOM_SEED = "randomSeed";
	private final static String JSON_ATTR_RECTANGLE_SIMULATION = "bbox";
	

	private String stationsFileName;
	private String entryPointsFileName;
	private String configSimulationFileName;
	
	public ConfigJsonReader(String stationsFileName, String entryPointsFileName, String configSimulationFileName) {
		this.stationsFileName = stationsFileName;
		this.entryPointsFileName = entryPointsFileName;
		this.configSimulationFileName = configSimulationFileName;
	}
	
	public SystemConfiguration readJson() throws FileNotFoundException {

		SystemConfiguration systemConfig = new SystemConfiguration();
		
		Gson gson = createAndConfigureGson(systemConfig);
		
		//Configuration
		readGlobalConfigurations(gson, systemConfig);

		//Stations
		readStations(gson, systemConfig);
		
		//EntryPoints
		readEntryPoints(gson, systemConfig);
		
		return systemConfig;
	}
	
	
	private Gson createAndConfigureGson(SystemConfiguration systemConfig) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		IdGenerator bikeIdGen = systemConfig.getBikeIdGen();
		IdGenerator stationIdGen = systemConfig.getStationIdGen();
		gsonBuilder.registerTypeAdapter(Station.class, new StationDeserializer(bikeIdGen, stationIdGen));
		gsonBuilder.registerTypeAdapter(EntryPoint.class, new EntryPointDeserializer());
		Gson gson = gsonBuilder.create();
		return gson;
	}

	private void readGlobalConfigurations(Gson gson, SystemConfiguration systemConfig) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(configSimulationFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		JsonObject jsonConfig = gson.fromJson(bufferedReader, JsonObject.class);
		systemConfig.setReservationTime(jsonConfig.get(JSON_ATTR_TIME_RESERVE).getAsInt());
		systemConfig.setTotalTimeSimulation(jsonConfig.get(JSON_ATTR_TIME_SIMULATION).getAsInt());
		systemConfig.setRandomSeed(jsonConfig.get(JSON_ATTR_RANDOM_SEED).getAsLong());
		StaticRandom.setSeed(systemConfig.getRandomSeed());
		JsonElement rectangleJson = jsonConfig.get(JSON_ATTR_RECTANGLE_SIMULATION).getAsJsonObject();
		systemConfig.setBoundingBox(gson.fromJson(rectangleJson, BoundingBox.class));
		systemConfig.setConfigStationPath(stationsFileName);
		systemConfig.setConfigEntryPath(entryPointsFileName);
		systemConfig.setConfigSimulationPath(configSimulationFileName);
		
	}
	
	private void readStations(Gson gson, SystemConfiguration systemConfig) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(stationsFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		
		ArrayList<Station> allStations = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_STATION).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			allStations.add(gson.fromJson(elemStation, Station.class));
		}
		systemConfig.setStations(allStations);
		systemConfig.getStations().stream().forEach(s -> systemConfig.getBikes().addAll(s.getBikes()));
	}
	
	private void readEntryPoints(Gson gson, SystemConfiguration systemConfig) throws FileNotFoundException {
		FileInputStream inputStreamJson = new FileInputStream(new File(entryPointsFileName));
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
		
		ArrayList<EntryPoint> allEntryPoints = new ArrayList<>();
		JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
				.get(JSON_ATTR_ENTRYPOINTS).getAsJsonArray();
		for(JsonElement elemStation: jsonStationsArray) {
			EntryPoint newEntryPoint = gson.fromJson(elemStation, EntryPoint.class);
			allEntryPoints.add(newEntryPoint);
		}
		for(EntryPoint entryPoint: allEntryPoints) {
			systemConfig.setEventUserAppears(entryPoint.generateEvents(systemConfig));
		}
	}
	

}
