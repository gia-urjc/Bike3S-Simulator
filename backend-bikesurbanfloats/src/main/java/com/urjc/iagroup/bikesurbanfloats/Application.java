package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;


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
			for(Station s: ConfigInfo.stations) {
				System.out.println(s.toString());
			}
			for(EntryPoint e: ConfigInfo.entryPoints) {
				System.out.println(e.toString());
			}
			System.out.println(ConfigInfo.reservationTime);
			System.out.println(ConfigInfo.totalTimeSimulation);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
