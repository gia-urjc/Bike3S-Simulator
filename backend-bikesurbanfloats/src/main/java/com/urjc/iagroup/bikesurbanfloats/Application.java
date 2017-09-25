package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.util.MathDistributions;

/**
 * Hello world!
 *
 */

public class Application {
	

	
    public static void main(String[] args) {
        ConfigJsonReader jsonReader = new ConfigJsonReader("configuration/config_bikes_number.json");
        ConfigInfo config;
		try {
			config = jsonReader.readJson();
			System.out.println(config.toString());
			for(EntryPoint e: config.getEntryPoints()) {
				e.generateEvents(10000);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
