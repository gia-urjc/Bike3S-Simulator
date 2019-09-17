package es.urjc.ia.bikesurbanfleets.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
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
    private static String mapPath;
    private static String demandDataPath;
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
        // String projectDir = System.getProperty("user.dir") + File.separator + "Bike3S";
 //       testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/newVersion/tests/utilityYsurr";
        //testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/newVersion/tests/utilityYsurroundWithDemand";
 //       testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/version_usersmax600/cost_complex_prediction";
        testsDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/cost_complex_prediction";
        mapPath = projectDir + "/../madrid.osm";
        demandDataPath = projectDir + "/../demandDataMadrid0817_0918.csv";
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

    private void executeTests(String testFile) throws FileNotFoundException, IOException, InterruptedException, GraphHopperIntegrationException, GeoRouteCreationException {
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
        String usersConfig = testsDir + "/conf/users_configuration.json";
        String stationsConfig = testsDir + "/conf/stations_configuration.json";
        ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
        GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
        
        //modify globalinfo with other parameters
        globalInfo.setOtherGraphParameters(mapPath);
        globalInfo.setOtherDemandDataFilePath(demandDataPath);

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

            runSimulationTest(globalInfo, jsonReader, testdir, t.getAsJsonObject("userType"), t.getAsJsonObject("recommendationSystemType"));
            runResultAanalisis(testdir);
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
            globalInfo.setOtherRecommendationSystemType(recomendertype);

            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            //modify user type specification with the one from the test
            List<JsonObject> users = usersInfo.getUsers();
            for (JsonObject user : users) {
                user.remove("userType");
                user.add("userType", usertype);
            }

            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();

            //3. do simulation
            //TODO mapPath not obligatory for other graph managers
            if (mapPath != null) {
                new SimulationEngine(globalInfo, stationsInfo, usersInfo);
            } else {
                MessageGuiFormatter.showErrorsForGui("You should specify a map directory");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void runResultAanalisis(String testdir) throws IOException, InterruptedException, GraphHopperIntegrationException, GeoRouteCreationException {
        SimulationResultAnalyser sra=new SimulationResultAnalyser(analisisDir+testdir, historyDir+testdir);
        sra.analyzeSimulation();
   }

    private void runscriptR() throws IOException, InterruptedException {

        List<String> command = new ArrayList<String>();
        command.add(analysisScriptPath + "./generateMarkdown.sh");
        command.add(analysisScriptPath);
        command.add(analisisDir + "report.html");
        command.add(analisisDir);

        System.out.println("\nexecuting: " + command.toString().replaceAll(",", ""));
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(analisisDir));

        Process p = pb.start(); // Start the process.
        p.waitFor(); // Wait for the process to finish.

        //Obtengo la salida de la ejecución del proceso
        System.out.println("----------------------------------------");
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        String resultExecution = builder.toString();
        System.out.println(resultExecution);
        System.out.println("----------------------------------------");

        System.out.println("Script executed successfully");
    }

}
