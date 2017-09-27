package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
<<<<<<< Updated upstream
import com.urjc.iagroup.bikesurbanfloats.config.ConfigJacksonReader;
=======
import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.MathDistributions;
>>>>>>> Stashed changes

/**
 * Hello world!
 *
 */

public class Application {
	

	
    public static void main(String[] args) {
<<<<<<< Updated upstream
        ConfigJacksonReader jsonReader = new ConfigJacksonReader("configuration/config_bikes_number.json");
        ConfigInfo config;
		try {
			config = jsonReader.readJson();
			config.toString();
		} catch (FileNotFoundException e) {
=======
        ConfigJsonReader jsonReader = new ConfigJsonReader("configuration/config_stations.json",
        		"configuration/config_entry_points.json", "configuration/config_simulation.json");
		try {
			int acum = 0;
			for(int i = 1; i < 100; i++) {
				double lambda = (double) 1 / 10;
				int poissonNumber = MathDistributions.poissonRandomInterarrivalDelay((double) 1 / lambda);
				acum += poissonNumber;
				System.out.println(poissonNumber);
			}
			System.out.println("==");
			System.out.println(acum/100);
			
		} catch (Exception e) {
>>>>>>> Stashed changes
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
