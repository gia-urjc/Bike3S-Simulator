package com.urjc.iagroup.bikesurbanfloats;

import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
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
			SystemInfo.randomSeed = 5;
			SystemInfo.random = new Random(SystemInfo.randomSeed);
			RandomUtil random = new RandomUtil();
			for(int i = 0; i < 100; i++) {
				System.out.println(random.nextInt(1, 5));
			}
			for(int i = 0; i < 100; i++) {
				System.out.println(random.nextDouble(Double.MIN_VALUE, 1.0));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
