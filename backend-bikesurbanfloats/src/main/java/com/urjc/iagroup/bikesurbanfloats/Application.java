package com.urjc.iagroup.bikesurbanfloats;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigJsonReader;
import com.urjc.iagroup.bikesurbanfloats.config.JsonValidation;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.core.SimulationEngine;
import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */

public class Application {
	
	
	private static CommandLine commandParser(String[] args) throws ParseException {
    	
    	Options options = new Options();
    	options.addOption("schema", true, "Directory to schema validation");
    	options.addOption("config", true, "Directory to the configuration file");
    	options.addOption("validator", true, "Directory to the js validator");
	
    	CommandLineParser parser = new DefaultParser();
    	return parser.parse(options, args);
		
	}
	
    public static void main(String[] args) throws ParseException {
    	
    	CommandLine cmd;
    	try {
			cmd = commandParser(args);
		} catch (ParseException e1) {
			System.out.println("Error reading params");
			throw e1;
		}
    	
    	String schema = cmd.getOptionValue("schema");
    	String config = cmd.getOptionValue("config");
    	String validator = cmd.getOptionValue("validator");
    	
    	if(checkParams(schema, config, validator)) {
    		String configurationFile = config;
            ConfigJsonReader jsonReader = new ConfigJsonReader(configurationFile);
            try {
    			SimulationConfiguration simulationConfiguration = jsonReader.createSimulationConfiguration();
    			SystemManager systemManager = jsonReader.createSystemManager(simulationConfiguration);
    			SimulationEngine simulation = new SimulationEngine(simulationConfiguration, systemManager);
    			simulation.run();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
	}

	private static boolean checkParams(String schema, String config, String validator) {
		if(validator != null && schema != null && config != null) {
    		try {
				String output = JsonValidation.validate(schema, config, validator);
				if(!output.equals("OK")) {
					System.out.println("JSON has errors");
					System.out.println(output);
					return false;
				} else {
					System.out.println("JSON is OK");
				}	
			} catch (IOException | InterruptedException e) {
				System.out.println("Fail executing validation");
				e.printStackTrace();
			}
    	}
    	else if(config == null) {
    		System.out.println("You should specify a configuration file");
    		return false;
    	}
		return true;
	}
    
}
