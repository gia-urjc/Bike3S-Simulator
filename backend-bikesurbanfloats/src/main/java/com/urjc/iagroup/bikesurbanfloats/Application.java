package com.urjc.iagroup.bikesurbanfloats;


import java.util.Arrays;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.Route;

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
			GeoPoint point1 = new GeoPoint(40.298778, -3.843014);
			GeoPoint point2 = new GeoPoint(40.298792, -3.842031);
			GeoPoint point3 = new GeoPoint(40.299516, -3.842055);
			GeoPoint point4 = new GeoPoint(40.300003, -3.842052);
			GeoPoint point5 = new GeoPoint(40.300355, -3.842055);
			
		    List<GeoPoint> listPoints = Arrays.asList(point1, point2, point3, point4, point5);
		    
		    Route route = new Route(listPoints);
		    System.out.println(route.calculateSubRoute(40, 2.5));
        	
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
}
