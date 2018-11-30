/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class ResultsComparator {

    private String analysisdir;
    private String outputFile;
    private int totalsimtime;

    public ResultsComparator(String analysisdir, String outputFile, int totalsimtime) {
        this.analysisdir = analysisdir;
        this.outputFile = outputFile;
        this.totalsimtime = totalsimtime;
    }

    public void compareTestResults() throws IOException {
        this.analysisdir = analysisdir;
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
            analyzeUsers(test, res.userdata);
            analyzeEmptyStations(test, res.stationdata);
            analyzeStationBalancing(test, res.stationdata);
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

        WiteUserdata(testresults, csvWriter);
        WiteStationdata(testresults, csvWriter);
        writer.close();
    }

    private void WitGeneraldata(CSVWriter csvWriter) throws IOException {
        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //Now set the String array for writing
        String[] record = {"total simulation time (min)", Double.toString((double) totalsimtime / 60D)};
        //write header
        csvWriter.writeNext(record);
    }

    private void WiteUserdata(TreeMap<String, TestResult> testresults, CSVWriter csvWriter) throws IOException {
        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //get max value of fails
        int maxrentfails = 0;
        int maxreturnfails = 0;
        for (TestResult tr : testresults.values()) {
            if (tr.userdata.usertakefails.lastKey() > maxrentfails) {
                maxrentfails = tr.userdata.usertakefails.lastKey();
            }
            if (tr.userdata.userreturnfails.lastKey() > maxreturnfails) {
                maxreturnfails = tr.userdata.userreturnfails.lastKey();
            }
        }

        //Now set the String array for writing
        String[] record = new String[10 + maxrentfails + maxreturnfails + 2];

        //setup header
        record[0] = "Testname";
        record[1] = "#users";
        record[2] = "DS";
        record[3] = "HE";
        record[4] = "RE";
        record[5] = "Av. time to station (min)";
        record[6] = "Av. time from orig to dest station (min)";
        record[7] = "Av. time to final destination (min)";
        record[8] = "Av. total time (min)";
        record[9] = "# abandoned";
        int i = 0;
        while (i <= maxrentfails) {
            record[10 + i] = "# with " + i + " rental fails";
            i++;
        }
        int j = 0;
        while (j <= maxreturnfails) {
            record[10 + i + j] = "# with " + j + " return fails";
            j++;
        }
        //write header
        csvWriter.writeNext(record);

        //now write the test results
        for (String t : testresults.keySet()) {
            TestResult res = testresults.get(t);
            for (int k = 0; k < 10 + maxrentfails + maxreturnfails + 2; k++) {
                record[k] = "";
            }
            record[0] = t;
            record[1] = Integer.toString(res.userdata.totalusersr);
            record[2] = Double.toString(res.userdata.DS);
            record[3] = Double.toString(res.userdata.HE);
            record[4] = Double.toString(res.userdata.RE);
            record[5] = Double.toString(res.userdata.avtostationtime / 60D);
            record[6] = Double.toString(res.userdata.avbetweenstationtime / 60D);
            record[7] = Double.toString(res.userdata.avfromstationtime / 60D);
            record[8] = Double.toString((res.userdata.avfromstationtime + res.userdata.avbetweenstationtime + res.userdata.avtostationtime) / 60D);
            record[9] = Integer.toString(res.userdata.avabandonos);
            for (Integer key : res.userdata.usertakefails.keySet()) {
                record[10 + key] = Integer.toString(res.userdata.usertakefails.get(key));
            }
            for (Integer key : res.userdata.userreturnfails.keySet()) {
                record[10 + maxrentfails + 1 + key] = Integer.toString(res.userdata.userreturnfails.get(key));
            }
            //write line
            csvWriter.writeNext(record);
        }
    }

    private void WiteStationdata(TreeMap<String, TestResult> testresults, CSVWriter csvWriter) throws IOException {

        // Write empty line
        csvWriter.writeNext(new String[]{""});
        //write header
        String[] record = {"test", "#stations", "#stations with empty times", "sum emptytimes all stations(min)", "average equilibrium dev. over all stations and total time (bikes)"};
        csvWriter.writeNext(record);

        int numstationwithemtytimes = 0;
        double totalemptytimes = 0;
        int totalstations = 0;
        double totaldeviationfromequilibrium = 0;

        //now write the test results
        for (String t : testresults.keySet()) {
            TestResult res = testresults.get(t);
            for (int k = 0; k < 5; k++) {
                record[k] = "";
            }
            record[0] = t;
            record[1] = Integer.toString(res.stationdata.totalstations);
            record[2] = Integer.toString(res.stationdata.numstationwithemtytimes);
            record[3] = Double.toString(res.stationdata.totalemptytimes / 60D);
            record[4] = Double.toString((res.stationdata.totaldeviationfromequilibrium)
                    / ((double) res.stationdata.totalstations));
            //write line
            csvWriter.writeNext(record);
        }
    }

    private class TestResult {

        StationData stationdata = new StationData();
        UserData userdata = new UserData();
    }

    private class StationData {

        int numstationwithemtytimes = 0;
        double totalemptytimes = 0;
        int totalstations = 0;
        double totaldeviationfromequilibrium = 0;
    }

    private class UserData {

        double avtostationtime = 0;
        int totalusersr = 0;
        double avbetweenstationtime = 0;
        double avfromstationtime = 0;
        int avabandonos = 0;
        double DS=0.0D;
        double HE=0.0D;
        double RE=0.0D;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
    }

    private void analyzeUsers(String test, UserData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/users.csv");
        double totaltostationtime = 0;
        int tostationcounter = 0;
        double totalbetweenstationtime = 0;
        int betweenstationcounter = 0;
        double totalfromstationtime = 0;
        int fromstationcounter = 0;
        int totalabandonos = 0;
        int totalusers=0;
        int failedRentalsUsersWithBike=0;
        int failedResturnsUsersWithBike=0;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
        for (String[] line : data) {
            totalusers++;
            if (line[4].equals("EXIT_AFTER_REACHING_DESTINATION")) {
                if (!(line[9].equals("1")) || !(line[11].equals("1"))) {
                    throw new RuntimeException("error in results");
                }
                tostationcounter++;
                totaltostationtime += Integer.parseInt(line[1]);
                if (!(line[2].equals(""))) {
                    betweenstationcounter++;
                    totalbetweenstationtime += Integer.parseInt(line[2]);
                }
                if (!(line[3].equals(""))) {
                    fromstationcounter++;
                    totalfromstationtime += Integer.parseInt(line[3]);
                }
                failedRentalsUsersWithBike+=Integer.parseInt(line[10]);
                Integer current = usertakefails.get(Integer.parseInt(line[10]));
                if (current == null) {
                    usertakefails.put(Integer.parseInt(line[10]), 1);
                } else {
                    usertakefails.put(Integer.parseInt(line[10]), current + 1);
                }
                failedResturnsUsersWithBike+=Integer.parseInt(line[12]);
                current = userreturnfails.get(Integer.parseInt(line[12]));
                if (current == null) {
                    userreturnfails.put(Integer.parseInt(line[12]), 1);
                } else {
                    userreturnfails.put(Integer.parseInt(line[12]), current + 1);
                }
            } else { //abandonados
                if (!(line[9].equals("0")) || !(line[11].equals("0"))) {
                    throw new RuntimeException("error in results");
                }
                totalabandonos++;
            }
        }
        if (tostationcounter!=betweenstationcounter || tostationcounter!=fromstationcounter ){
                     throw new RuntimeException("error in results");           
        }
        dat.avtostationtime = totaltostationtime / ((double) tostationcounter);
        dat.totalusersr = totalusers ;
        dat.avbetweenstationtime = totalbetweenstationtime / ((double) betweenstationcounter);
        dat.avfromstationtime = totalfromstationtime / ((double) fromstationcounter);
        dat.avabandonos = totalabandonos;
        dat.usertakefails = usertakefails;
        dat.userreturnfails = userreturnfails;
        dat.DS=((double) (tostationcounter))/((double)totalusers);
        dat.HE=((double) (tostationcounter))/((double)(tostationcounter+failedRentalsUsersWithBike));
        dat.RE=((double) (tostationcounter))/((double)(tostationcounter+failedResturnsUsersWithBike));
   }

    private void analyzeEmptyStations(String test, StationData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/empty_stations.csv");
        for (String[] line : data) {
            dat.totalstations++;
            if (!(line[2].equals("0"))) {
                dat.numstationwithemtytimes++;
                dat.totalemptytimes += Integer.parseInt(line[2]);
            }
        }
    }

    private void analyzeStationBalancing(String test, StationData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(analysisdir + test + "/stationBalanceQuality.csv");
        int totalstations = 0;
        for (String[] line : data) {
            totalstations++;
            dat.totaldeviationfromequilibrium += Double.valueOf(line[1]);
        }
        if (totalstations != dat.totalstations) {
            throw new RuntimeException("error in results");
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
                        .withSkipLines(1)
                        .withCSVParser(parser)
                        .build();
        List<String[]> allData = csvReader.readAll();

        return allData;
    }

}
