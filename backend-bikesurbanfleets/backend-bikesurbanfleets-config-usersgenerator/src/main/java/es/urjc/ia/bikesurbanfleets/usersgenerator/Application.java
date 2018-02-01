package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.EntryPointInfo;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;
import jdk.nashorn.internal.objects.Global;
import org.apache.commons.cli.*;

public class Application {

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";


    private static CommandLine commandParser(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption("entryPointsSchema", true, "Directory to entry points schema validation");
        options.addOption("globalSchema", true, "Directory to global schema");
        options.addOption("entryPointsInput", true, "Directory to the input entry points configuration file");
        options.addOption("globalInput", true, "Directory to the input entry points configuration file");
        options.addOption("output", true, "Directory to the output users configuration file");
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

        String entryPointSchema = cmd.getOptionValue("entryPointsSchema");
        String globalSchema = cmd.getOptionValue("globalSchema");
        String entryPointInput = cmd.getOptionValue("entryPointsInput");
        String globalInput = cmd.getOptionValue("globalInput");
        String configOutput = cmd.getOptionValue("output");
        String validator = cmd.getOptionValue("validator");
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
                SimulationRandom.init(globalInfo.getRandomSeed());
                configurationIO.writeFinalConfig(entryPointInput, configOutput, entryPointInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(entryPointInput != null && globalInput != null && configOutput != null) {
            configurationIO = new ConfigurationIO();
            try {
                entryPointInfo = configurationIO.readPreConfigEntryPoints(entryPointInput);
                globalInfo = configurationIO.readPreConfigGlobalInfo(globalInput);
                EntryPoint.TOTAL_SIMULATION_TIME = globalInfo.getTotalSimulationTime();
                SimulationRandom.init(globalInfo.getRandomSeed());
                configurationIO.writeFinalConfig(entryPointInput, configOutput, entryPointInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println(ANSI_RED + "Arguments are not set correctly" + ANSI_RESET);
            System.out.println("Example without validation: 'java -jar <PATH_TO_JAR>/usersgenerator.jar -configInput <CONFIG_INPUT_PATH> -configOutput <CONFIG_OUTPUT_PATH>'");
            System.out.println("Example with validation: java -jar '<PATH_TO_JAR>/usersgenerator.jar -configInput <CONFIG_INPUT_PATH> -configOutput <CONFIG_OUTPUT_PATH> -schema <PATH_SCHEMAS> -validator <VALIDATOR_PATH>'");
        }


    }

}
