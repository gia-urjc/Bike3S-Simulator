package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.core.config.ConfigJsonReader;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.StationsInfo;
import es.urjc.ia.bikesurbanfleets.core.config.UsersInfo;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;

import java.io.IOException;

import es.urjc.ia.bikesurbanfleets.systemmanager.SystemManager;
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
    
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    
    
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
        
        String globalSchema = cmd.getOptionValue("global-schema");
        String usersSchema = cmd.getOptionValue("global-schema");
        String stationsSchema = cmd.getOptionValue("stations-schema");
        String globalConfig = cmd.getOptionValue("global-config");
        String usersConfig = cmd.getOptionValue("users-config");
        String stationsConfig = cmd.getOptionValue("stations-config");
        String validator = cmd.getOptionValue("validator");
        
        if(checkParams(globalSchema, usersSchema, stationsSchema, globalConfig, usersConfig, stationsConfig, validator)) {
            ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
            try {
                GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
                UsersInfo usersInfo = jsonReader.readUsersConfiguration();
                StationsInfo stationsInfo = jsonReader.readStationsConfiguration();
                SystemManager systemManager = jsonReader.createSystemManager(stationsInfo, globalInfo);
                SimulationEngine simulation = new SimulationEngine(globalInfo, stationsInfo, usersInfo, systemManager);
                simulation.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean checkParams(String globalSchema, String usersSchema, String stationsSchema, String globalConfig, String usersConfig, String stationsConfig, String validator) {
        boolean result = false;
        if(globalSchema != null && usersSchema != null && stationsSchema != null
                && globalConfig != null && stationsConfig != null && validator != null) {
            try {

                String globalConfigValidation = JsonValidation.validate(globalSchema, globalConfig, validator);
                String usersConfigValidation = JsonValidation.validate(usersSchema, usersConfig, validator);
                String stationsConfigValidation = JsonValidation.validate(stationsSchema, usersConfig, validator);

                if((!globalConfigValidation.equals("OK") || !usersConfigValidation.equals("OK") || !stationsConfigValidation.equals("OK"))
                        && (!globalConfig.equals("NODE_NOT_INSTALLED"))) {
                    System.out.println(ANSI_RED +"JSON has errors" + ANSI_RESET);
                    System.out.println(ANSI_RED + "Global configuration errors" + ANSI_RESET);
                    System.out.println(globalConfigValidation);
                    System.out.println(ANSI_RED + "Stations configuration errors" + ANSI_RESET);
                    System.out.println(stationsConfigValidation);
                    System.out.println(ANSI_RED + "Users configuration errors" + ANSI_RESET);
                    System.out.println(usersConfigValidation);
                    result = true;
                    return false;
                } else if (globalConfigValidation.equals("NODE_NOT_INSALLED")) {
                    System.out.println(ANSI_RED + "Node is necessary to execute validator: " + validator + ". \n"
                            + "Verify if node is installed or install node" + ANSI_RESET);
                } else if(globalConfigValidation.equals("OK") && stationsConfigValidation.equals("OK") && usersConfigValidation.equals("OK")) {
                    System.out.println(ANSI_GREEN + "Validation configuration input: OK" + ANSI_RESET);
                    result = true;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println(ANSI_RED + "Fail executing validation" + ANSI_RESET);
                e.printStackTrace();
            }
        }
        else if(globalConfig == null || stationsConfig == null || usersConfig == null) {
            System.out.println(ANSI_RED + "You should specify a configuration file" + ANSI_RESET);
            result = false;
        }
        else if((globalSchema == null || usersSchema == null || stationsSchema == null) && validator != null) {
            System.out.println(ANSI_RED + "You should specify all schema paths" + ANSI_RESET);
            result = false;
        }
        else if(validator == null) {
            System.out.println(ANSI_YELLOW + "Warning, you don't specify a validator, configuration file will not be validated"
                    + " on backend" + ANSI_RESET);
            result = true;
        }
        return result;
    }
    
}
