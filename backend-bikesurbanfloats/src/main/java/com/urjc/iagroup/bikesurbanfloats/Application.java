package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;

import java.io.FileNotFoundException;

/**
 * Hello world!
 *
 */

public class Application {
	
    public static void main(String[] args) {
    	String stationsPath = args[0];
    	String entryPath = args[1];
    	String simulationPath = args[2];
        ConfigJsonReader jsonReader = new ConfigJsonReader(stationsPath, entryPath, simulationPath);
		
        try {
			SimulationConfiguration simulationConfiguration = jsonReader.createSystemConfiguration();
			SystemManager systemManager = jsonReader.createSystemManager();
			SimulationEngine simulation = new SimulationEngine(simulationConfiguration, systemManager);
			simulation.processEntryPoints();
			simulation.run();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    }
}
