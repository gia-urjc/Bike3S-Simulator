/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.core;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class ResultsComparator {

    public void compareTestResults(String analysisdir, String outputFile) throws IOException {
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
        writeCVS(testresults);
    }

    private void writeCVS(TreeMap<String, TestResult> testresults){
        
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
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
    }

    private static void analyzeUsers(String test, UserData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(test + "users.cvs");
        double totaltostationtime = 0;
        int tostationcounter = 0;
        double totalbetweenstationtime = 0;
        int betweenstationcounter = 0;
        double totalfromstationtime = 0;
        int fromstationcounter = 0;
        int totalabandonos = 0;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
        for (String[] line : data) {
            tostationcounter++;
            totaltostationtime += Integer.getInteger(line[1]);
            if (!(line[2].equals(""))) {
                betweenstationcounter++;
                totalbetweenstationtime += Integer.getInteger(line[1]);
            }
            if (!(line[3].equals(""))) {
                fromstationcounter++;
                totalfromstationtime += Integer.getInteger(line[1]);
            }
            if (!(line[4].equals("EXIT_AFTER_REACHING_DESTINATION"))) {
                if (!(line[9].equals("0")) || !(line[11].equals("0"))) {
                    throw new RuntimeException("error in results");
                }
                totalabandonos++;
            } else if (!(line[9].equals("1")) || !(line[11].equals("1"))) {
                throw new RuntimeException("error in results");
            }
            Integer current = usertakefails.get(Integer.getInteger(line[10]));
            if (current == null) {
                usertakefails.put(Integer.getInteger(line[10]), 1);
            } else {
                usertakefails.put(Integer.getInteger(line[10]), current + 1);
            }
            current = userreturnfails.get(Integer.getInteger(line[12]));
            if (current == null) {
                userreturnfails.put(Integer.getInteger(line[12]), 1);
            } else {
                userreturnfails.put(Integer.getInteger(line[12]), current + 1);
            }
        }
        dat.avtostationtime = totaltostationtime / ((double) tostationcounter);
        dat.totalusersr = tostationcounter;
        dat.avbetweenstationtime = totalbetweenstationtime / ((double) betweenstationcounter);
        dat.avfromstationtime = totalfromstationtime / ((double) fromstationcounter);
        dat.avabandonos = totalabandonos;
        dat.usertakefails = usertakefails;
        dat.userreturnfails = userreturnfails;
    }

    private static void analyzeEmptyStations(String test, StationData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(test + "empty_stations.cvs");
        for (String[] line : data) {
            dat.totalstations++;
            if (!(line[2].equals("0"))) {
                dat.numstationwithemtytimes++;
                dat.totalemptytimes += Integer.getInteger(line[2]);
            }
        }
    }

    private static void analyzeStationBalancing(String test, StationData dat) throws IOException {
        List<String[]> data = readAllDataAtOnce(test + "stationBalancingQuality.cvs");
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

        // create csvReader object and skip first Line 
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withSkipLines(1)
                .build();
        List<String[]> allData = csvReader.readAll();

        return allData;
    }

}
