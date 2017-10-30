package com.urjc.iagroup.bikesurbanfloats.config;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.EntryPointDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.deserializers.StationDeserializer;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

import java.io.*;
import java.util.ArrayList;

public class ConfigJsonReader {

    private final static String JSON_ATTR_STATION = "stations";
    private final static String JSON_ATTR_ENTRYPOINTS = "entryPoints";
    private final static String JSON_ATTR_TIME_RESERVE = "reservationTime";
    private final static String JSON_ATTR_TIME_SIMULATION = "totalTimeSimulation";
    private final static String JSON_ATTR_RANDOM_SEED = "randomSeed";
    private final static String JSON_ATTR_RECTANGLE_SIMULATION = "bbox";
    private final static String JSON_ATTR_MAP_DIRECTORY = "mapDirectory";
    private final static String JSON_ATTR_GRAPHHOPPER_DIRECORY = "graphhopperDirectory";
    private final static String JSON_ATTR_GRAPHHOPPER_LOCALE = "graphHopperLocale";

    private String configurationFile;

    private Gson gson;

    public ConfigJsonReader(String configurationFile) {
    	this.configurationFile = configurationFile;
        this.gson = createAndConfigureGson();
    }

    public SimulationConfiguration createSystemConfiguration() throws FileNotFoundException {

        SimulationConfiguration simulationConfiguration = new SimulationConfiguration();

        //Configuration
        readGlobalConfigurations(simulationConfiguration);

        //EntryPoints
        readEntryPoints(simulationConfiguration);

        return simulationConfiguration;
    }

    public SystemManager createSystemManager(SimulationConfiguration simulationConfiguration) throws IOException {
        FileInputStream inputStreamJson = new FileInputStream(new File(configurationFile));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));

        ArrayList<Station> allStations = new ArrayList<>();
        JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
        		.get(JSON_ATTR_STATION).getAsJsonArray();
        for (JsonElement elemStation : jsonStationsArray) {
            allStations.add(gson.fromJson(elemStation, Station.class));
        }
        return new SystemManager(allStations, simulationConfiguration);
    }


    private Gson createAndConfigureGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Station.class, new StationDeserializer());
        gsonBuilder.registerTypeAdapter(EntryPoint.class, new EntryPointDeserializer());
        Gson gson = gsonBuilder.create();
        return gson;
    }

    private void readGlobalConfigurations(SimulationConfiguration simulationConfiguration) throws FileNotFoundException {
        FileInputStream inputStreamJson = new FileInputStream(new File(configurationFile));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));
        JsonObject jsonConfig = gson.fromJson(bufferedReader, JsonObject.class);
        Reservation.VALID_TIME = jsonConfig.get(JSON_ATTR_TIME_RESERVE).getAsInt();
        EntryPoint.TOTAL_TIME_SIMULATION = jsonConfig.get(JSON_ATTR_TIME_SIMULATION).getAsInt();
        simulationConfiguration.setRandomSeed(jsonConfig.get(JSON_ATTR_RANDOM_SEED).getAsLong());
        StaticRandom.createRandom(simulationConfiguration.getRandomSeed());
        JsonElement rectangleJson = jsonConfig.get(JSON_ATTR_RECTANGLE_SIMULATION).getAsJsonObject();
        simulationConfiguration.setBoundingBox(gson.fromJson(rectangleJson, BoundingBox.class));
        simulationConfiguration.setMapDirectory(jsonConfig.get(JSON_ATTR_MAP_DIRECTORY).getAsString());
        simulationConfiguration.setGraphHopperLocale(jsonConfig.get(JSON_ATTR_GRAPHHOPPER_LOCALE).getAsString());
        simulationConfiguration.setConfigurationFile(configurationFile);
        
        
    }

    private void readEntryPoints(SimulationConfiguration simulationConfiguration) throws FileNotFoundException {
        FileInputStream inputStreamJson = new FileInputStream(new File(configurationFile));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamJson));

        ArrayList<EntryPoint> allEntryPoints = new ArrayList<>();
        JsonArray jsonStationsArray = gson.fromJson(bufferedReader, JsonObject.class)
                .get(JSON_ATTR_ENTRYPOINTS).getAsJsonArray();
        for (JsonElement elemStation : jsonStationsArray) {
            EntryPoint newEntryPoint = gson.fromJson(elemStation, EntryPoint.class);
            allEntryPoints.add(newEntryPoint);
        }
        for (EntryPoint entryPoint : allEntryPoints) {
            simulationConfiguration.setEventUserAppears(entryPoint.generateEvents());
        }
    }


}
