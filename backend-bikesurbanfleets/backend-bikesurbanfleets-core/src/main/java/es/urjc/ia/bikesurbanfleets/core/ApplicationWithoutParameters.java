package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation.ValidationParams;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
import es.urjc.ia.bikesurbanfleets.core.exceptions.ValidationException;
import es.urjc.ia.bikesurbanfleets.resultanalysis.SimulationResultAnalyser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
 



public class ApplicationWithoutParameters {

    //Program parameters
    private static String globalConfig;
    private static String usersConfig;
    private static String stationsConfig;
    private static String historyOutputPath;
    private static String analysisOutputPath;
   
    public static void main(String[] args) throws Exception {

        String projectDir= System.getProperty("user.dir") + File.separator;

        String test="Bike3STests/paperAT2018/prueba";
        
        String basedir=projectDir+test;
        GlobalInfo.DEBUG_DIR=basedir+ "/debug";
        System.out.println("Test:"+test);

        //Create auxiliary folders
        File auxiliaryDir = new File(GlobalInfo.TEMP_DIR);
        if(!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        auxiliaryDir = new File(GlobalInfo.DEBUG_DIR);
        if(!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        globalConfig = basedir +"/conf/global_configuration.json";
        usersConfig = basedir+ "/conf/users_configuration.json";
        stationsConfig = basedir+ "/conf/stations_configuration.json";
        historyOutputPath = basedir+ "/hist";
        analysisOutputPath= basedir+"/analysis";

        try {
            
            //1.read global configuration (and setup some changes)
            //to do maybe change graph parameters into the global config file
            ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
            GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
            if(historyOutputPath != null) {
                globalInfo.setOtherHistoryOutputPath(historyOutputPath);
            }

            //2. read stations and user configurations
            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();

            //3. do simulation
            new SimulationEngine(globalInfo, stationsInfo, usersInfo);
            
            //4. analyse the simulation results
            SimulationResultAnalyser sra = new SimulationResultAnalyser(analysisOutputPath, historyOutputPath);
            sra.analyzeSimulation();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
