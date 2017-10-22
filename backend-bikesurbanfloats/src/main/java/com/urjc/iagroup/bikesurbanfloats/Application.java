package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
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
			SystemInfo systemInfo = jsonReader.readJson();
			SimulationEngine simulation = new SimulationEngine(systemInfo);
			simulation.processEntryPoints();
			simulation.run();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    }
}
