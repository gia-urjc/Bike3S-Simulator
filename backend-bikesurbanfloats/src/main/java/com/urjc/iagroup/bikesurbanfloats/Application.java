package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;


/**
 * Hello world!
 *
 */

public class Application {
	

	
    public static void main(String[] args) {
        ConfigJsonReader jsonReader = new ConfigJsonReader("configuration/config_stations.json",
        		"configuration/config_entry_points.json", "configuration/config_simulation.json");
		try {
			jsonReader.readJson();
			SimulationEngine simulation = new SimulationEngine();
			simulation.processConfig();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
