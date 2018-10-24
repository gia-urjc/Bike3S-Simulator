package es.urjc.ia.bikesurbanfleets.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
import es.urjc.ia.bikesurbanfleets.core.exceptions.ValidationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HolgerScript {

    private class Tests {

        String basedir;
        private List<JsonObject> tests;
    }

    //Program parameters
    private static String baseDir;
    private static String debugDir;
    private static String historyDir;
    private static String analisisDir;
    private static String baseTestsDir;
    private static String mapPath;
    private static String schemaPath;
    private static String dataAnalyzerPath;

    public static void main(String[] args) throws Exception {
        HolgerScript hs = new HolgerScript();
       //treat tests
        String testFile = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/Script/tests.json";
        mapPath = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/madrid.osm";
        schemaPath = "/Users/holger/workspace/BikeProjects/Bike3S/build/schema";
        dataAnalyzerPath="/Users/holger/workspace/BikeProjects/Bike3S/build/data-analyser";
        hs.executeTests(testFile);
    }

    private void executeTests( String testFile) throws FileNotFoundException, IOException, InterruptedException {
        //Create auxiliary folders
        File auxiliaryDir = new File(GlobalInfo.TEMP_DIR);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }

        Gson gson = new Gson();
        FileReader reader = new FileReader(testFile);
        Tests tests = gson.fromJson(reader, Tests.class);
        baseDir=tests.basedir;
        //create new dir on basedir
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss");
        Date date = new Date();
        baseTestsDir = baseDir + "/"+ dateFormat.format(date);
        auxiliaryDir = new File(baseTestsDir);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        debugDir = baseTestsDir + "/debug/";
        auxiliaryDir = new File(debugDir);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        historyDir = baseTestsDir + "/history/";
        auxiliaryDir = new File(historyDir);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        analisisDir = baseTestsDir + "/analisis/";
        auxiliaryDir = new File(analisisDir);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }

        //now loop through the tests
        ArrayList<String> testnames = new ArrayList<String>();
        for (JsonObject t : tests.tests) {
            String usertype = t.getAsJsonObject("userType").get("typeName").getAsString();
            String recomendertype = t.getAsJsonObject("recommendationSystemType").get("typeName").getAsString();
            String testdir = usertype + "_" + recomendertype;
            int i = 0;
            while (exists(testdir + i, testnames)) {
                i++;
            }
            testdir = testdir + i;
            testnames.add(testdir);
            runSimulationTest(testdir, t.getAsJsonObject("userType"), t.getAsJsonObject("recommendationSystemType"));
            runResultAanalisis(testdir);
        }
    }

    private boolean exists(String name, List<String> names) {
        for (String s:names) {
            if (s.equals(name)) 
                return true;
        }
        return false;
    }

    private void runSimulationTest(String testdir, JsonObject usertype, JsonObject recomendertype) {
        //Create auxiliary folders
        GlobalInfo.DEBUG_DIR = debugDir + "/"+ testdir;
        File auxiliaryDir = new File(GlobalInfo.DEBUG_DIR);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        String globalConfig = baseDir + "/conf/global_configuration.json";
        String usersConfig = baseDir + "/conf/users_configuration.json";
        String stationsConfig = baseDir + "/conf/stations_configuration.json";
        String historyOutputPath = historyDir + "/" + testdir;
        auxiliaryDir = new File(historyOutputPath);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }

        ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);

        try {
            GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
            //modify recomenderspecification with the one from the test
            globalInfo.setRecommendationSystemType(recomendertype);
            
            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            //modify user type specification with the one from the test
            List<JsonObject> users=usersInfo.getUsers();
            for (JsonObject user: users){
                user.remove("userType");
                user.add("userType", usertype);
            }
            
            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();
            System.out.println("DEBUG MODE: " + globalInfo.isDebugMode());
            if (historyOutputPath != null) {
                globalInfo.setHistoryOutputPath(historyOutputPath);
            }

            if (mapPath != null) {
                SimulationEngine simulation = new SimulationEngine(globalInfo, stationsInfo, usersInfo, mapPath);
                simulation.run();
            } else {
                MessageGuiFormatter.showErrorsForGui("You should specify a map directory");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    
    private void runResultAanalisis(String testdir) throws IOException, InterruptedException {
         File auxiliaryDir = new File(analisisDir + testdir);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        
        List<String> command = new ArrayList<String>();
        command.add("node");
        command.add(dataAnalyzerPath +"/data-analyser.js");
        command.add("analyse");
        command.add("-h");
        command.add(historyDir +  testdir);
        command.add("-s");
        command.add(schemaPath);
        command.add("-c");
        command.add(analisisDir + testdir);
      
        
        for (String s: command) {
            System.out.print(s + " ");
        }
        System.out.println();
        ProcessBuilder pb = new ProcessBuilder(command);
        
        Process p = pb.start(); // Start the process.
        p.waitFor(); // Wait for the process to finish.
        System.out.println("Script executed successfully");
    }

}
