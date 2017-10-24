package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

/**
 * Hello world!
 *
 */

public class Application {
	
    public static void main(String[] args) {
    	//String stationsPacth = args[0];
    	//String entryPath = args[1];
    	//String simulationPath = args[2];
        //ConfigJsonReader jsonReader = new ConfigJsonReader(stationsPacth, 
        //		entryPath, simulationPath);
    	
        try {
			//SystemConfiguration systemConfig = jsonReader.readJson();
			//SimulationEngine simulation = new SimulationEngine(systemConfig);
			//simulation.processEntryPoints();
			//simulation.run();
			GeoPoint point1 = new GeoPoint(40.3425485, -3.7729534);
			GeoPoint point2 = new GeoPoint(40.3414385, -3.7701223);
			
			System.out.println(point1.reachedPoint(200, point2));
        	
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
}
