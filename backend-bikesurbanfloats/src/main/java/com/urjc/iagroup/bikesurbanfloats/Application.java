package com.urjc.iagroup.bikesurbanfloats;

import java.io.FileNotFoundException;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigJacksonReader;

/**
 * Hello world!
 *
 */

public class Application {
	

	
    public static void main(String[] args) {
        ConfigJacksonReader jsonReader = new ConfigJacksonReader("configuration/config_bikes_number.json");
        ConfigInfo config;
		try {
			config = jsonReader.readJson();
			config.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
