package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.Gson;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.ConfigurationIO;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.EntryPointList;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint;
import org.apache.commons.cli.*;

public class Application {

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";


    private static CommandLine commandParser(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption("schema", true, "Directory to schema validation");
        options.addOption("configInput", true, "Directory to the input configuration file");
        options.addOption("configOutput", true, "Directory to the output configuration file");
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
        String configInput = cmd.getOptionValue("configInput");
        String configOutput = cmd.getOptionValue("configOutput");
        String validator = cmd.getOptionValue("validator");
        ConfigurationIO configurationIO;
        EntryPointList entryPointList;
        Gson gson = new Gson();
        if(schema != null && validator != null && configInput != null && configOutput != null){
            configurationIO = new ConfigurationIO(validator, schema);
            try {
                entryPointList = configurationIO.readPreConfigEntryPoints(configInput);
                EntryPoint.TOTAL_SIMULATION_TIME = entryPointList.getTotalSimulationTime();
                SimulationRandom.init(entryPointList.getRandomSeed());
                configurationIO.writeFinalConfig(configInput, configOutput, entryPointList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(configInput != null && configOutput != null) {
            configurationIO = new ConfigurationIO();
            try {
                entryPointList = configurationIO.readPreConfigEntryPoints(configInput);
                EntryPoint.TOTAL_SIMULATION_TIME = entryPointList.getTotalSimulationTime();
                SimulationRandom.init(entryPointList.getRandomSeed());
                configurationIO.writeFinalConfig(configInput, configOutput, entryPointList);
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
