package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;
import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;


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
			System.out.println(SystemInfo.strInfo());
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    }
}
