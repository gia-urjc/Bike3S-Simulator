/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.resultanalysis.ManagerDataAnalyzer.GlobalManagerDataForExecution;
import es.urjc.ia.bikesurbanfleets.resultanalysis.StationDataAnalyzer.GlobalStationDataForExecution;
import es.urjc.ia.bikesurbanfleets.resultanalysis.UserDataAnalyzer.GlobalUserDataForExecution;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class ResultsComparator {

    private String analysisdir;
    private String historydir;
    private String outputFile;
    private int totalsimtime;

    public ResultsComparator(String analysisdir, String historydir, String outputFile, int totalsimtime) {
        this.analysisdir = analysisdir;
        this.historydir = historydir;
        this.outputFile = outputFile;
        this.totalsimtime = totalsimtime;
    }

    public void compareTestResults() throws IOException {
        //copy the script file to the directory
        Path analysispath = new File(analysisdir).toPath();
        File file = new File(analysisdir);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // new go through the directories
        TreeMap<String, TestResult> testresults = new TreeMap<>();
        for (String test : directories) {
            TestResult res = new TestResult();
            testresults.put(test, res);
            readRecommenderParameters(test, res);
            analyzeUsers(test, res.userdata);
            analyzeStations(test, res.stationdata);
            analyzeManager(test, res.managerdata);
        }
        writeCVS(testresults, outputFile);
    }

    private void writeCVS(TreeMap<String, TestResult> testresults, String outputfile) throws IOException {

        //set the writer
        Writer writer = new FileWriter(outputfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        WriteGeneraldata(csvWriter);
        WriteUserdata(testresults, csvWriter);
        WriteStationdata(testresults, csvWriter);
        WriteManagerdata(testresults, csvWriter);
        writer.close();
    }

    private void WriteGeneraldata(CSVWriter csvWriter) {
        //write empty line
        csvWriter.writeNext(new String[]{""});

        String[] desc = new String[3];
        desc[0] = "simulation time (min)";
        desc[1] = Double.toString((double) totalsimtime / 60D);
        desc[2] = "the data correspond to values up to this time (users, changes and any events after that time are ignored; also users that do not appear in this tiem are ignored)";
        csvWriter.writeNext(desc);
    }

    private void WriteUserdata(TreeMap<String, TestResult> testresults, CSVWriter csvWriter) throws IOException {
        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //get max value of fails
        int maxrentfails = 0;
        int maxreturnfails = 0;
        for (TestResult tr : testresults.values()) {
            if (!tr.userdata.usertakefails.isEmpty() && tr.userdata.usertakefails.lastKey() > maxrentfails) {
                maxrentfails = tr.userdata.usertakefails.lastKey();
            }
            if (!tr.userdata.userreturnfails.isEmpty() && tr.userdata.userreturnfails.lastKey() > maxreturnfails) {
                maxreturnfails = tr.userdata.userreturnfails.lastKey();
            }
        }

        //Now set the String array for writing
        String[] record = new String[15 + maxrentfails + maxreturnfails + 2];

        //setup header
        record[0] = "Testname";
        record[1] = "recommerderParameters";
        record[2] = "#users total (up to simulationtime)";
        record[3] = "#users finished in simulationtime";
        record[4] = "#abandoned";
        record[5] = "DS";
        record[6] = "HE";
        record[7] = "RE";
        record[8] = "Av. time to station (min)(only succesfull users)";
        record[9] = "Av. time from orig to dest station (min)(only succesfull users)";
        record[10] = "Av. time to final destination (min)(only succesfull users)";
        record[11] = "Av. total time (min)";
        record[12] = "Av. timeloss (min)";
        record[13] = "#failed rentals (only succesfull users)";
        record[14] = "#failed returns (only succesfull users)";
        int i = 0;
        while (i <= maxrentfails) {
            record[15 + i] = "# with " + i + " rental fails";
            i++;
        }
        int j = 0;
        while (j <= maxreturnfails) {
            record[15 + i + j] = "# with " + j + " return fails";
            j++;
        }
        //write header
        csvWriter.writeNext(record);

        //now write the test results
        for (String t : testresults.keySet()) {
            TestResult res = testresults.get(t);
            for (int k = 0; k < 15 + maxrentfails + maxreturnfails + 2; k++) {
                record[k] = "";
            }
            record[0] = t;
            record[1] = res.recommenderParameters;
            record[2] = Integer.toString(res.userdata.totalusers);
            record[3] = Integer.toString(res.userdata.finishedusers);
            record[4] = Integer.toString(res.userdata.avabandonos);
            record[5] = Double.toString(res.userdata.DS);
            record[6] = Double.toString(res.userdata.HE);
            record[7] = Double.toString(res.userdata.RE);
            record[8] = Double.toString(res.userdata.avtostationtime / 60D);
            record[9] = Double.toString(res.userdata.avbetweenstationtime / 60D);
            record[10] = Double.toString(res.userdata.avfromstationtime / 60D);
            record[11] = Double.toString((res.userdata.avfromstationtime + res.userdata.avbetweenstationtime + res.userdata.avtostationtime) / 60D);
            record[12] = Double.toString(res.userdata.avtimeloss);
            record[13] = Integer.toString(res.userdata.totalfailedrentals);
            record[14] = Integer.toString(res.userdata.totalfailedreturns);
            for (Integer key : res.userdata.usertakefails.keySet()) {
                record[15 + key] = Integer.toString(res.userdata.usertakefails.get(key));
            }
            for (Integer key : res.userdata.userreturnfails.keySet()) {
                record[15 + maxrentfails + 1 + key] = Integer.toString(res.userdata.userreturnfails.get(key));
            }
            //write line
            csvWriter.writeNext(record);
        }
    }

    private void WriteStationdata(TreeMap<String, TestResult> testresults, CSVWriter csvWriter) throws IOException {

        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //write header
        String[] record = {"Testname", "recommerderParameters", "#stations", "#stations with empty times", "Av. empty times (min)", "Av. equilibrium desviation (over stations and simulationtime)"};
        csvWriter.writeNext(record);

        //now write the test results
        for (String t : testresults.keySet()) {
            TestResult res = testresults.get(t);
            for (int k = 0; k < record.length; k++) {
                record[k] = "";
            }
            record[0] = t;
            record[1] = res.recommenderParameters;
            record[2] = Integer.toString(res.stationdata.totalstations);
            record[3] = Integer.toString(res.stationdata.numstationwithemtytimes);
            record[4] = Double.toString((res.stationdata.totalemptytimes) / (res.stationdata.totalstations));
            record[5] = Double.toString((res.stationdata.totaldeviationfromequilibrium)
                    / ((double) res.stationdata.totalstations));
            //write line
            csvWriter.writeNext(record);
        }
    }

    private void WriteManagerdata(TreeMap<String, TestResult> testresults, CSVWriter csvWriter) throws IOException {
        //First get a complete map of events
        //get max value of fails
        int index = 3;
        TreeMap<String, Integer> allevents = new TreeMap<String, Integer>();
        for (TestResult tr : testresults.values()) {
            if (!tr.managerdata.manager_event_data.isEmpty()) {
                for (String ev : tr.managerdata.manager_event_data.keySet()) {
                    if (allevents.get(ev) == null) {
                        allevents.put(ev, index);
                        index = index + 3;
                    }
                }
            }
        }

        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //write header
        String[] record = new String[index];
        record[0] = "Testname";
        record[1] = "recommerderParameters";
        record[2] = "#total managing events";
        for (String ev : allevents.keySet()) {
            int position = allevents.get(ev);
            record[position] = ev + " #all";
            record[position + 1] = ev + " #success";
            record[position + 2] = ev + " #failed";
        }
        csvWriter.writeNext(record);

        //now write the test results
        for (String t : testresults.keySet()) {
            TestResult res = testresults.get(t);
            for (int k = 0; k < record.length; k++) {
                record[k] = "";
            }
            record[0] = t;
            record[1] = res.recommenderParameters;
            record[2] = Integer.toString(res.managerdata.totalevents);
            for (String ev : res.managerdata.manager_event_data.keySet()) {
                int[] dat = res.managerdata.manager_event_data.get(ev);
                int position = allevents.get(ev);
                record[position] = Integer.toString(dat[0]);
                record[position + 1] = Integer.toString(dat[1]);
                record[position + 2] = Integer.toString(dat[2]);
            }

            //write line
            csvWriter.writeNext(record);
        }
    }
    

    static private class TestResult {
        GlobalStationDataForExecution stationdata = new GlobalStationDataForExecution();
        GlobalUserDataForExecution userdata = new GlobalUserDataForExecution();
        GlobalManagerDataForExecution managerdata = new GlobalManagerDataForExecution();
        String recommenderParameters;
    }

    private void readRecommenderParameters(String test, TestResult res) throws FileNotFoundException, IOException {
        String filename = historydir + test + "/simulation_parameters.json";
        res.recommenderParameters = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
    }

    private void analyzeUsers(String test, GlobalUserDataForExecution dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/users.csv");
        double totaltostationtime = 0;
        double totalbetweenstationtime = 0;
        double totalfromstationtime = 0;
        int totalabandonos = 0;
        int totalusers = 0;
        int usersfinishedintime = 0;
        double totaltimeloss = 0;
        int failedRentalsUsersWithBike = 0;
        int failedResturnsUsersWithBike = 0;
        int usersreacheddestination = 0;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
        for (String[] line : data) {
            totalusers++;
            if (line[1].equals("yes")) {
                usersfinishedintime++;
                if (line[8].equals("EXIT_AFTER_REACHING_DESTINATION")) {
                    usersreacheddestination++;
                    if (!(line[13].equals("1")) || !(line[15].equals("1"))) {
                        throw new RuntimeException("error in results");
                    }
                    totaltostationtime += Integer.parseInt(line[3]) - Integer.parseInt(line[2]);
                    totalbetweenstationtime += Integer.parseInt(line[4]) - Integer.parseInt(line[3]);
                    totalfromstationtime += Integer.parseInt(line[5]) - Integer.parseInt(line[4]);
                    failedRentalsUsersWithBike += Integer.parseInt(line[14]);
                    Integer current = usertakefails.get(Integer.parseInt(line[14]));
                    if (current == null) {
                        usertakefails.put(Integer.parseInt(line[14]), 1);
                    } else {
                        usertakefails.put(Integer.parseInt(line[14]), current + 1);
                    }
                    failedResturnsUsersWithBike += Integer.parseInt(line[16]);
                    current = userreturnfails.get(Integer.parseInt(line[16]));
                    if (current == null) {
                        userreturnfails.put(Integer.parseInt(line[16]), 1);
                    } else {
                        userreturnfails.put(Integer.parseInt(line[16]), current + 1);
                    }
                    totaltimeloss += Double.parseDouble(line[7]);
                } else { //abandonados
                    if (!(line[13].equals("0")) || !(line[15].equals("0"))) {
                        throw new RuntimeException("error in results");
                    }
                    totalabandonos++;
                }
            }
        }
        dat.totalusers = totalusers;
        dat.finishedusers = usersfinishedintime;
        dat.avtostationtime = totaltostationtime / ((double) usersreacheddestination);
        dat.avbetweenstationtime = totalbetweenstationtime / ((double) usersreacheddestination);
        dat.avfromstationtime = totalfromstationtime / ((double) usersreacheddestination);
        dat.totalfailedrentals = failedRentalsUsersWithBike;
        dat.totalfailedreturns = failedResturnsUsersWithBike;
        dat.avabandonos = totalabandonos;
        dat.usertakefails = usertakefails;
        dat.userreturnfails = userreturnfails;
        dat.DS = ((double) (usersreacheddestination)) / ((double) usersfinishedintime);
        dat.HE = ((double) (usersreacheddestination)) / ((double) (usersreacheddestination + failedRentalsUsersWithBike));
        dat.RE = ((double) (usersreacheddestination)) / ((double) (usersreacheddestination + failedResturnsUsersWithBike));
        dat.avtimeloss = totaltimeloss / ((double) usersreacheddestination);
    }

    private void analyzeManager(String test, GlobalManagerDataForExecution dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/fleetManager.csv");

        dat.manager_event_data = new TreeMap<String, int[]>();
        dat.totalevents = 0;

        for (String[] line : data) {
            dat.totalevents++;
            String name = line[1];
            int[] v = dat.manager_event_data.get(name);
            if (v == null) {
                int[] v2 = {0, 0, 0};
                v = v2;
                dat.manager_event_data.put(name, v);
            }
            String resut = line[3];
            v[0] = v[0] + 1;
            if (resut.equals(Event.RESULT_TYPE.SUCCESS.toString())) {
                v[1] = v[1] + 1;
            } else if (resut.equals(Event.RESULT_TYPE.FAIL.toString())) {
                v[2] = v[2] + 1;
            } else {
                throw new RuntimeException("resulttype not allowed");
            }
        }
    }

    private void analyzeStations(String test, GlobalStationDataForExecution dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/stations.csv");
        for (String[] line : data) {
            dat.totalstations++;
            double empty = Double.valueOf(line[1]);
            if (empty > 0) {
                dat.numstationwithemtytimes++;
            }
            dat.totalemptytimes += empty;
            dat.totaldeviationfromequilibrium += Double.valueOf(line[2]);
        }
    }

    public static List<String[]> readAllDataAtOnce(String file) throws FileNotFoundException, IOException {
        // Create an object of file reader 
        // class with CSV file as a parameter. 
        FileReader filereader = new FileReader(file);
        CSVParser parser
                = new CSVParserBuilder()
                .withSeparator(';')
                .build();
        CSVReader csvReader
                = new CSVReaderBuilder(filereader)
                .withSkipLines(2)
                .withCSVParser(parser)
                .build();
        List<String[]> allData = csvReader.readAll();

        return allData;
    }

}
