package es.urjc.ia.bikesurbanfleets.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
import es.urjc.ia.bikesurbanfleets.resultanalysis.ResultsComparator;
import es.urjc.ia.bikesurbanfleets.resultanalysis.SimulationResultAnalyser;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompareTestApplication {

    private class Tests {

        private List<JsonObject> tests;
    }

    //Program parameters
    private static String testsDir;
    private static String debugDir;
    private static String historyDir;
    private static String analisisDir;
    private static String baseTestsDir;
    private static String schemaPath;
    private static String dataAnalyzerPath;
    private static String analysisScriptPath;

    public static void main(String[] args) throws Exception {
        CompareTestApplication hs = new CompareTestApplication();
        //treat tests
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // the following parameters may have to be changes
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String projectDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3S-Simulator";
        
    //    testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/prueba";
        testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests_20_7_2018_7_ usersrandom_halfbikes/cost_prediction_new";
    //    testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/SimulationJournalEvaluationTest/evaluationTest_withmanager";
         //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        System.out.println("baseDir " + testsDir);
        String testFile = testsDir + "/tests.json";
        schemaPath = projectDir + "/build/schema";
        dataAnalyzerPath = projectDir + "/build/data-analyser";
        analysisScriptPath = projectDir + "/tools/analysis-script/";
        hs.executeTests(testFile);
        System.out.println("tests execution finished");
        System.gc();
    }

    private void executeTests(String testFile) throws Exception {
        //Create auxiliary folders
        File auxiliaryDir = new File(GlobalInfo.TEMP_DIR);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }

        Gson gson = new Gson();
        FileReader reader = new FileReader(testFile);
        Tests tests = gson.fromJson(reader, Tests.class);
        //create new dir on basedir
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

        Date date = new Date();
        baseTestsDir = testsDir + "/" + dateFormat.format(date);
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

        String globalConfig = testsDir + "/conf/global_configuration.json";
        String stationsConfig = testsDir + "/conf/stations_configuration.json";
        String usersConfig = testsDir + "/conf/users_configuration.json";
            
        ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
        GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();

        //now loop through the tests
        ArrayList<String> testnames = new ArrayList<String>();
        for (JsonObject t : tests.tests) {
            JsonObject userob = t.getAsJsonObject("userType");
            String usertype;
            if (userob == null || userob.get("typeName")==null) {
                usertype = "origin";
            } else {
                usertype = userob.get("typeName").getAsString();
            }
            JsonObject recomenderob = t.getAsJsonObject("recommendationSystemType");
            String recomendertype;
            if (recomenderob == null || recomenderob.get("typeName")==null) {
                recomendertype = "origin";
            } else {
                recomendertype = recomenderob.get("typeName").getAsString();
            }
            
            String testdir = usertype + "_" + recomendertype;
            int i = 0;
            while (exists(testdir + i, testnames)) {
                i++;
            }
            testdir = testdir + i;
            testnames.add(testdir);

            runSimulationTest(globalInfo, jsonReader, testdir, userob, recomenderob);
        }
        
        new ResultsComparator(analisisDir, historyDir, analisisDir + "compareResults.csv", globalInfo.getTotalSimulationTime()).compareTestResults();
        //script requires autorization    runscriptR();
    }

    private boolean exists(String name, List<String> names) {
        for (String s : names) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void runSimulationTest(GlobalInfo globalInfo, ConfigJsonReader jsonReader, String testdir, JsonObject usertype, JsonObject recomendertype) {
        //Create auxiliary folders
        GlobalInfo.DEBUG_DIR = debugDir + "/" + testdir;
        File auxiliaryDir = new File(GlobalInfo.DEBUG_DIR);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        String historyOutputPath = historyDir + "/" + testdir;
        auxiliaryDir = new File(historyOutputPath);
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        globalInfo.setOtherHistoryOutputPath(historyOutputPath);

        try {

            //modify recomenderspecification with the one from the test
            if (recomendertype != null && recomendertype.get("typeName")!=null) {
                globalInfo.setOtherRecommendationSystem(recomendertype);
            }
            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            //modify user type specification with the one from the test
            if (usertype!=null && usertype.get("typeName")!=null){
                List<JsonObject> users = usersInfo.getUsers();
                for (JsonObject user : users) {
                    //substitute the usertype
                    user.remove("userType");
                    user.add("userType", usertype);
                }
            }

            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();

            //3. do simulation
            new SimulationEngine(globalInfo, stationsInfo, usersInfo);

            //4. analyse the simulation results
            SimulationResultAnalyser sra = new SimulationResultAnalyser(analisisDir + testdir, historyDir + testdir);
            sra.analyzeSimulation();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
