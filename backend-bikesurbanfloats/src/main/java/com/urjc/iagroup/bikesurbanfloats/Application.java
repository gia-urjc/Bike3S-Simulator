package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;

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
			try {
			simulation.run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
