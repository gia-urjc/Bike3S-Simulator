package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;

import java.io.IOException;

/**
 * Hello world!
 *
 */

public class Application {
	
    public static void main(String[] args) {
    	String configurationFile = args[0];
        ConfigJsonReader jsonReader = new ConfigJsonReader(configurationFile);
        try {
			SimulationConfiguration simulationConfiguration = jsonReader.createSystemConfiguration();
			SystemManager systemManager = jsonReader.createSystemManager(simulationConfiguration);
			SimulationEngine simulation = new SimulationEngine(simulationConfiguration, systemManager);
			simulation.run();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
