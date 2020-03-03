package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.resultanalysis.SimulationResultAnalyser;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;

import java.io.File;
 



public class ApplicationWithoutParameters {

    //Program parameters
    private static String globalConfig;
    private static String usersConfig;
    private static String stationsConfig;
    private static String historyOutputPath;
    private static String analysisOutputPath;
   
    public static void main(String[] args) throws Exception {

        String testDir="/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/prueba2";
        GlobalConfigurationParameters.DEBUG_DIR=testDir+ "/debug";
        System.out.println("Test:"+testDir);

        //Create auxiliary folders
        File auxiliaryDir = new File(GlobalConfigurationParameters.TEMP_DIR);
        if(!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        auxiliaryDir = new File(GlobalConfigurationParameters.DEBUG_DIR);
        if(!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        globalConfig = testDir +"/conf/global_configuration.json";
        usersConfig = testDir+ "/conf/users_configuration.json";
        stationsConfig = testDir+ "/conf/stations_configuration.json";
        historyOutputPath = testDir+ "/hist";
        analysisOutputPath= testDir+"/analysis";

        try {
            
            //1.read global configuration (and setup some changes)
            //to do maybe change graph parameters into the global config file
            ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
            GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
            if(historyOutputPath != null) {
                globalInfo.setOtherHistoryOutputPath(historyOutputPath);
            }

            //2. load Graph Manager
            GraphManager graphManager=GraphManager.getGraphManager(globalInfo);

            //3. read stations and user configurations
            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();

            //4. do simulation
            new SimulationEngine(globalInfo, stationsInfo, usersInfo, graphManager);
            
            //5. analyse the simulation results
            SimulationResultAnalyser sra = new SimulationResultAnalyser(analysisOutputPath, historyOutputPath,graphManager);
            sra.analyzeSimulation();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
