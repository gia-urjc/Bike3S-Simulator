package es.urjc.ia.bikesurbanfleets.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.*;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Bike;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.io.*;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HolgerScript {

    private class Tests {

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
    private static String analysisScriptPath;
    private static int totalsimulationtime;

    public static void main(String[] args) throws Exception {
        HolgerScript hs = new HolgerScript();
        //treat tests
        String projectDir="/Users/holger/workspace/BikeProjects/Bike3S8.0/tests/";
        //String projectDir= System.getProperty("user.dir") + File.separator;

        baseDir=projectDir+"tests_holger_rec";
        
        
        System.out.println("baseDir " + baseDir);
        String testFile = baseDir+"/tests.json";
        mapPath = projectDir+"backend-configuration-files/maps/madrid.osm";
        schemaPath = projectDir+"build/schema";
        dataAnalyzerPath=projectDir+"build/data-analyser";
        analysisScriptPath=projectDir+"Bike3STests/analysis_scripts/";
        hs.executeTests(testFile);
        System.out.println("tests execution finished");
        System.gc();
    }

    private void executeTests(String testFile) throws FileNotFoundException, IOException, InterruptedException {
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
        baseTestsDir = baseDir + "/" + dateFormat.format(date);
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

        String globalConfig = baseDir + "/conf/global_configuration.json";
        String usersConfig = baseDir + "/conf/users_configuration.json";
        String stationsConfig = baseDir + "/conf/stations_configuration.json";
        ConfigJsonReader jsonReader = new ConfigJsonReader(globalConfig, stationsConfig, usersConfig);
        GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
        totalsimulationtime=globalInfo.getTotalSimulationTime();

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

        runscriptR();

        new ResultsComparator(analisisDir, analisisDir + "compareResults.csv", totalsimulationtime).compareTestResults();

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
        globalInfo.setHistoryOutputPath(historyOutputPath);

        try {
            //set up the global variables and singleton classes
            Bike.resetIdGenerator();
            Station.resetIdGenerator();
            User.resetIdGenerator();
            Reservation.resetIdGenerator();

            //now initialize history and debug 
            Debug.init(globalInfo.isDebugMode());
            System.out.println("DEBUG MODE: " + Debug.isDebugmode());

            History.init(globalInfo.getHistoryOutputPath());

            //modify recomenderspecification with the one from the test
            globalInfo.setRecommendationSystemType(recomendertype);

            UsersConfig usersInfo = jsonReader.readUsersConfiguration();
            //modify user type specification with the one from the test
            List<JsonObject> users = usersInfo.getUsers();
            for (JsonObject user : users) {
                user.remove("userType");
                user.add("userType", usertype);
            }

            StationsConfig stationsInfo = jsonReader.readStationsConfiguration();

            if (mapPath != null) {
                SimulationEngine simulation = new SimulationEngine(globalInfo, stationsInfo, usersInfo, mapPath);
                simulation.run();
            } else {
                MessageGuiFormatter.showErrorsForGui("You should specify a map directory");
            }
            Debug.close();
            History.close();

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
        command.add(dataAnalyzerPath + "/data-analyser.js");
        command.add("analyse");
        command.add("-h");
        command.add(historyDir + testdir);
        command.add("-s");
        command.add(schemaPath);
        command.add("-c");
        command.add(analisisDir + testdir);

        System.out.println("executing: node " + dataAnalyzerPath + "/data-analyser.js analyse -h " + historyDir + testdir
                + " -s " + schemaPath + " -c " + analisisDir + testdir);
        ProcessBuilder pb = new ProcessBuilder(command);

        Process p = pb.start(); // Start the process.
        p.waitFor(25,TimeUnit.SECONDS); // Wait for the process to finish.
        p.destroy();
        System.out.println("Script executed successfully");
    }
    
   private void runscriptR() throws IOException, InterruptedException {

       List<String> command = new ArrayList<String>();
       command.add(analysisScriptPath+"./generateMarkdown.sh");
       command.add(analysisScriptPath);
       command.add(analisisDir+"report.html");
       command.add(analisisDir);

       System.out.println("\nexecuting: " + command.toString().replaceAll(",",""));
       ProcessBuilder pb = new ProcessBuilder(command);
       pb.directory(new File(analisisDir));

       Process p = pb.start(); // Start the process.
        p.waitFor(25,TimeUnit.SECONDS); // Wait for the process to finish.
        p.destroy();

       //Obtengo la salida de la ejecución del proceso
       System.out.println("----------------------------------------");
       BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
       StringBuilder builder = new StringBuilder();
       String line = null;
       while ( (line = reader.readLine()) != null) {
           builder.append(line);
           builder.append(System.getProperty("line.separator"));
       }

       String resultExecution = builder.toString();
       System.out.println(resultExecution);
       System.out.println("----------------------------------------");

       System.out.println("Script executed successfully");
  }

}
