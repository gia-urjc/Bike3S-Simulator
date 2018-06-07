package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation.ValidationParams;
import es.urjc.ia.bikesurbanfleets.core.config.ConfigJsonReader;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.StationsConfig;
import es.urjc.ia.bikesurbanfleets.core.config.UsersConfig;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;


import es.urjc.ia.bikesurbanfleets.core.exceptions.ValidationException;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import java.io.File;


public class Application {

    //Program parameters
    private static String globalSchema;
    private static String usersSchema;
    private static String stationsSchema;
    private static String globalConfig;
    private static String usersConfig;
    private static String stationsConfig;
    private static String historyOutputPath;
    private static String validator;
    private static boolean callFromFrontend;

    
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
        options.addOption("callFromFrontend", false, "Backend has been called by frontend");
    
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
        
    }
    
    public static void main(String[] args) throws Exception {

        //Create auxiliary folder
        File auxiliaryDir = new File(GlobalInfo.AUX_DIR);
        FileUtils.deleteDirectory(auxiliaryDir);
        auxiliaryDir.mkdirs();

        CommandLine cmd;
        try {
            cmd = commandParser(args);
        } catch (ParseException e1) {
            System.out.println("Error reading params");
            throw e1;
        }

        globalSchema = cmd.getOptionValue("globalSchema");
        usersSchema = cmd.getOptionValue("usersSchema");
        stationsSchema = cmd.getOptionValue("stationsSchema");
        globalConfig = cmd.getOptionValue("globalConfig");
        usersConfig = cmd.getOptionValue("usersConfig");
        stationsConfig = cmd.getOptionValue("stationsConfig");
        historyOutputPath = cmd.getOptionValue("historyOutput");
        validator = cmd.getOptionValue("validator");
        callFromFrontend = cmd.hasOption("callFromFrontend");
        
        checkParams(); // If not valid, throws exception
        ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);

        try {
            GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();
            System.out.println("DEBUG MODE: " + globalInfo.isDebugMode());
            if(historyOutputPath != null) {
                globalInfo.setHistoryOutputPath(historyOutputPath);
            }
            InfraestructureManager systemManager = jsonReader.createSystemManager(stationsInfo, globalInfo);
            SimulationEngine simulation = new SimulationEngine(globalInfo, stationsInfo, usersInfo, systemManager);
            simulation.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void checkParams() throws Exception {

        String exMessage = null; // Message for exceptions
        String warningMessage = null;
        if(hasAllSchemasAndConfig()) {

            ValidationParams vParams = new ValidationParams();
            vParams.setSchemaDir(globalSchema).setJsonDir(globalConfig).setJsValidatorDir(validator);
            String globalConfigValidation = validate(vParams);

            vParams.setSchemaDir(usersSchema).setJsonDir(usersConfig);
            String usersConfigValidation = validate(vParams);

            vParams.setSchemaDir(stationsSchema).setJsonDir(stationsConfig);
            String stationsConfigValidation = validate(vParams);

            System.out.println(globalConfigValidation);
            System.out.println(usersConfigValidation);
            System.out.println(stationsConfigValidation);

            if((!globalConfigValidation.equals("OK")
                    || !usersConfigValidation.equals("OK") || !stationsConfigValidation.equals("OK"))) {

                exMessage = "JSON has errors \n Global configuration errors \n" + globalConfigValidation + "\n" +
                        "Stations configuration errors \n" + stationsConfigValidation + "\n" +
                        "Users configuration errors \n" + usersConfigValidation;

            } else if (globalConfigValidation.equals("NODE_NOT_INSALLED")) {

                exMessage = "Node is necessary to execute validator: " + validator + ". \n" +
                        "Verify if node is installed or install node";

            } else if(globalConfigValidation.equals("OK") && stationsConfigValidation.equals("OK")
                    && usersConfigValidation.equals("OK")) {

                System.out.println("Validation configuration input: OK\n");
            }
        }
        else if(globalConfig == null || stationsConfig == null || usersConfig == null) {
            exMessage = "You should specify a configuration file";
        }
        else if((globalSchema == null || usersSchema == null || stationsSchema == null) && validator != null) {
            exMessage = "You should specify all schema paths";

        }
        else if(validator == null && !callFromFrontend) {
            warningMessage = "Warning: you don't specify a validator, configuration file will not be validated on backend";
        }

        if(exMessage != null) {
            System.out.println("Exception");
            throw new ValidationException(exMessage);
        }

        if(warningMessage != null) {
            System.out.println(warningMessage);
        }
    }

    private static boolean hasAllSchemasAndConfig() {
        return globalSchema != null && usersSchema != null && stationsSchema != null && globalConfig != null
                && stationsConfig != null && validator != null;
    }

    private static String validate(ValidationParams vParams) throws Exception {
        return JsonValidation.validate(vParams);
    }

}
