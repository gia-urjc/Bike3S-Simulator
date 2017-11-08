package com.urjc.iagroup.bikesurbanfloats.config;

import com.google.gson.Gson;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

import java.io.FileReader;
import java.io.IOException;

public class ConfigJsonReader {

    private String configurationFile;

    private Gson gson;

    public ConfigJsonReader(String configurationFile) {
    	this.configurationFile = configurationFile;
        this.gson = new Gson();
    }

    public SimulationConfiguration createSimulationConfiguration() throws IOException {
        try (FileReader reader = new FileReader(configurationFile)) {
            SimulationConfiguration simulationConfiguration = gson.fromJson(reader, SimulationConfiguration.class);

            StaticRandom.init(simulationConfiguration.getRandomSeed());
            EntryPoint.TOTAL_SIMULATION_TIME = simulationConfiguration.getTotalSimulationTime();
            Reservation.VALID_TIME = simulationConfiguration.getReservationTime();

            return simulationConfiguration;
        }
    }

    public SystemManager createSystemManager(SimulationConfiguration simulationConfiguration) throws IOException {
        return new SystemManager(simulationConfiguration);
    }

}
