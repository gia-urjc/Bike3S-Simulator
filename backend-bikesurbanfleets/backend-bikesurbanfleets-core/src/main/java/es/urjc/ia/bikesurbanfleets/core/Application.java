package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.core.config.ConfigJsonReader;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
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
        options.addOption("globalSchema", true, "Directory to global schema validation");
        options.addOption("usersSchema", true, "Directory to users schema validation");
        options.addOption("stationsSchema", true, "Directory to stations schema validation");
        options.addOption("globalConfig", true, "Directory to the global configuration file");
        options.addOption("usersConfig", true, "Directory to the users configuration file");
        options.addOption("stationsConfig", true, "Directory to the stations configuration file");
        options.addOption("historyOutput", true, "History Path for the simulation");
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
        
        String globalSchema = cmd.getOptionValue("globalSchema");
        String usersSchema = cmd.getOptionValue("usersSchema");
        String stationsSchema = cmd.getOptionValue("stationsSchema");
        String globalConfig = cmd.getOptionValue("globalConfig");
        String usersConfig = cmd.getOptionValue("usersConfig");
        String stationsConfig = cmd.getOptionValue("stationsConfig");
        String historyOutputPath = cmd.getOptionValue("historyOutput");
        String validator = cmd.getOptionValue("validator");
        
        if(checkParams(globalSchema, usersSchema, stationsSchema, globalConfig, usersConfig, stationsConfig, validator)) {
            ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
            try {
                GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
                UsersInfo usersInfo = jsonReader.readUsersConfiguration();
                StationsInfo stationsInfo = jsonReader.readStationsConfiguration();
                System.out.println("DEBUG MODE: " + globalInfo.isDebugMode());
                if(historyOutputPath != null) {
                    globalInfo.setHistoryOutputPath(historyOutputPath);
                }
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
                String stationsConfigValidation = JsonValidation.validate(stationsSchema, stationsConfig, validator);

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
            } catch (Exception e) {
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
