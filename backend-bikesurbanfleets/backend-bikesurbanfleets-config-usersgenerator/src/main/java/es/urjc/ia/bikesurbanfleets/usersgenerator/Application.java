package es.urjc.ia.bikesurbanfleets.usersgenerator;

import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.EntryPointInfo;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import org.apache.commons.cli.*;

public class Application {


    private static CommandLine commandParser(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption("entryPointsSchema", true, "Directory to entry points schema validation");
        options.addOption("globalSchema", true, "Directory to global schema");
        options.addOption("entryPointsInput", true, "Directory to the input entry points configuration file");
        options.addOption("globalInput", true, "Directory to the input entry points configuration file");
        options.addOption("output", true, "Directory to the output users configuration file");
        options.addOption("validator", true, "Directory to the js validator");
        options.addOption("callFromFrontend", false, "Backend has been called by frontend");

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

        String entryPointSchema = cmd.getOptionValue("entryPointsSchema");
        String globalSchema = cmd.getOptionValue("globalSchema");
        String entryPointInput = cmd.getOptionValue("entryPointsInput");
        String globalInput = cmd.getOptionValue("globalInput");
        String configOutput = cmd.getOptionValue("output");
        String validator = cmd.getOptionValue("validator");
        boolean callFromFrontend = cmd.hasOption("callFromFrontend");

        ConfigurationIO configurationIO;
        EntryPointInfo entryPointInfo;
        GlobalInfo globalInfo;
        if(entryPointSchema != null && globalSchema != null
                && validator != null && entryPointInput != null && globalInput != null && configOutput != null){
            configurationIO = new ConfigurationIO(validator, entryPointSchema, globalSchema);
            try {
                entryPointInfo = configurationIO.readPreConfigEntryPoints(entryPointInput);
                globalInfo = configurationIO.readPreConfigGlobalInfo(globalInput);
                EntryPoint.TOTAL_SIMULATION_TIME = globalInfo.getTotalSimulationTime();
                if(globalInfo.getRandomSeed() == 0) {
                    SimulationRandom.init();
                }
                else {
                    SimulationRandom.init(globalInfo.getRandomSeed());
                }
                configurationIO.writeFinalConfig(entryPointInput, configOutput, entryPointInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(entryPointInput != null && globalInput != null && configOutput != null) {
            configurationIO = new ConfigurationIO(callFromFrontend);
            try {
                entryPointInfo = configurationIO.readPreConfigEntryPoints(entryPointInput);
                globalInfo = configurationIO.readPreConfigGlobalInfo(globalInput);
                EntryPoint.TOTAL_SIMULATION_TIME = globalInfo.getTotalSimulationTime();
                if(globalInfo.getRandomSeed() == 0) {
                    SimulationRandom.init();
                }
                else {
                    SimulationRandom.init(globalInfo.getRandomSeed());
                }
                configurationIO.writeFinalConfig(entryPointInput, configOutput, entryPointInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Arguments are not set correctly");
            System.out.println("Example without validation: 'java -jar <PATH_TO_JAR>/usersgenerator.jar -configInput <CONFIG_INPUT_PATH> -configOutput <CONFIG_OUTPUT_PATH>'");
            System.out.println("Example with validation: java -jar '<PATH_TO_JAR>/usersgenerator.jar -configInput <CONFIG_INPUT_PATH> -configOutput <CONFIG_OUTPUT_PATH> -schema <PATH_SCHEMAS> -validator <VALIDATOR_PATH>'");
        }


    }

}
