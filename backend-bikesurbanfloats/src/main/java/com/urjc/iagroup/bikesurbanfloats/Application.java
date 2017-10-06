package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;

/**
 * Hello world!
 *
 */

public class Application {
	
	private static final String CONFIG_STATION_PATH = "configuration/config_stations.json";
	private static final String CONFIG_ENTRYP_PATH = "configuration/config_entry_points.json";
	private static final String CONFIG_SIMULATION_PATH = "configuration/config_simulation.json";
	
    public static void main(String[] args) {
        ConfigJsonReader jsonReader = new ConfigJsonReader(CONFIG_STATION_PATH, 
        		CONFIG_ENTRYP_PATH, CONFIG_SIMULATION_PATH);
		
        try {
			jsonReader.readJson();
			SimulationEngine simulation = new SimulationEngine();
			simulation.processConfig();
			simulation.run();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    }
}
