package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;

/**
 * Hello world!
 *
 */

public class Application {
	
    public static void main(String[] args) {
    	String stationsPacth = args[0];
    	String entryPath = args[1];
    	String simulationPath = args[2];
        ConfigJsonReader jsonReader = new ConfigJsonReader(stationsPacth, 
        		entryPath, simulationPath);
		
        try {
			SystemConfiguration systemInfo = jsonReader.readJson();
			SimulationEngine simulation = new SimulationEngine(systemInfo);
			simulation.processEntryPoints();
			simulation.run();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    }
}
