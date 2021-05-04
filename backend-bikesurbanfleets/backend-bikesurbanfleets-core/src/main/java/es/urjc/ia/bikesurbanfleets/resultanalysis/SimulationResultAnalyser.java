/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphHopperManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses.FinalGlobalValues;
import es.urjc.ia.bikesurbanfleets.resultanalysis.ManagerDataAnalyzer.GlobalManagerDataForExecution;
import es.urjc.ia.bikesurbanfleets.resultanalysis.StationDataAnalyzer.GlobalStationDataForExecution;
import es.urjc.ia.bikesurbanfleets.resultanalysis.UserDataAnalyzer.GlobalUserDataForExecution;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author holger
 */
public class SimulationResultAnalyser {

    public static void main(String[] args) throws Exception {
        String historydir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/history/2/";
        String analysisdir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/analysis/2/";
        String tempdir = System.getProperty("user.home") + "/.Bike3S";
        String mapdir = "/Users/holger/workspace/BikeProjects/Bike3S/madrid.osm";

        GraphManager gm = new GraphHopperManager(mapdir, tempdir);
        SimulationResultAnalyser sa = new SimulationResultAnalyser(analysisdir, historydir, gm);
        sa.analyzeSimulation();
    }
    static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    private Path analysispath;
    private Path historypath;
    private int totalsimtimespecified = 0;

    StationDataAnalyzer stationanalyzer;
    UserDataAnalyzer usernalyzer;
    ManagerDataAnalyzer manageranalyzer;
    StationOccupationAnalyzer stationoccanalyzer;

    public SimulationResultAnalyser(String analysisdir, String historydir, GraphManager routeService) throws IOException {
        this.analysispath = Paths.get(analysisdir);
        this.historypath = Paths.get(historydir);

        // read global values
        File json = historypath.resolve("final-global-values.json").toFile();
        FileReader red = new FileReader(json);
        FinalGlobalValues fgv = gson.fromJson(red, FinalGlobalValues.class);
        totalsimtimespecified = fgv.getTotalTimeSimulation();
        red.close();

        // setup analyzers 
        stationanalyzer = new StationDataAnalyzer();
        //read initial station data
        stationanalyzer.readInitialStationData(historypath.resolve("entities/stations.json"));
        usernalyzer = new UserDataAnalyzer(stationanalyzer.getStationData(), routeService);
        manageranalyzer = new ManagerDataAnalyzer();
        stationoccanalyzer = new StationOccupationAnalyzer(stationanalyzer.getStationData());

    }

    public void analyzeSimulation() throws Exception {

        //read history
        File file = new File(historypath.toString());
        File[] directories = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        LinkedList<File> filelist = new LinkedList<File>();
        for (File histname : directories) {
            if (histname.getName().matches("\\d+\\-\\d+\\_\\d+\\.json")) {
                int i = 0;
                int valuen = Integer.parseInt(histname.getName().substring(0, histname.getName().indexOf('-')));
                for (i = 0; i < filelist.size(); i++) {
                    int valueo = Integer.parseInt(filelist.get(i).getName().substring(0, filelist.get(i).getName().indexOf('-')));
                    if (valuen < valueo) {
                        break;
                    }
                }
                filelist.add(i, histname);
            }
        }
        for (File histname : filelist) {
            processHistoryEntries(readHistoryEntries(histname));
        }

        //postprocess data and write
        //write the results to files
        File auxiliaryDir = analysispath.toFile();
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }

        // changes some parts in the individual user and station data
        stationanalyzer.postProcessAndWrite(this.analysispath.resolve("stations.csv"), totalsimtimespecified);
        usernalyzer.postProcessAndWrite(this.analysispath.resolve("users.csv"), totalsimtimespecified);
        manageranalyzer.postProcessAndWrite(this.analysispath.resolve("fleetManager.csv"), totalsimtimespecified);
        stationoccanalyzer.postProcessAndWrite(this.analysispath.resolve("stationocupation.csv"), totalsimtimespecified);

        //now generate global information for a summary file
        WriteGeneraldata(stationanalyzer.getGlobalStationData(), 
                usernalyzer.getGlobalUserData(),
                manageranalyzer.getGlobalManagerData());
    }

    private void processHistoryEntries(HistoryJsonClasses.TimeEntry[] historyentries) {

        //find the entries
        for (HistoryJsonClasses.TimeEntry historyentry : historyentries) {
            int time = historyentry.getTime();

            //we only consider events that happened in the simulation time
            //should not be <= since an event at totalsimtimespecified is finished in the next second
            if (time < totalsimtimespecified) {
                for (HistoryJsonClasses.EventEntry ee : historyentry.getEvents()) {
                    //imprortant do not change the order!!!!!
                    stationoccanalyzer.analyzeEventEntryInTime(ee, time);
                    usernalyzer.analyzeEventEntryInTime(ee, time);
                    manageranalyzer.analyzeEventEntryInTime(ee, time);
                    stationanalyzer.analyzeEventEntryInTime(ee, time);
                }
           }
        }
    }

    static Integer getUserID(Collection<HistoryJsonClasses.IdReference> ent) {
        Integer ret = null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("users")) {
                if (ret != null) {
                    throw new RuntimeException("more than one user found in event");
                }
                ret = (Integer) ref.getId();
            }
        }
        return ret;
    }

    static Integer getStationID(Collection<HistoryJsonClasses.IdReference> ent) {
        Integer ret = null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("stations")) {
                if (ret != null) {
                    throw new RuntimeException("more than one station found in event");
                }
                ret = (Integer) ref.getId();
            }
        }
        return ret;
    }

    static Integer getManagerID(Collection<HistoryJsonClasses.IdReference> ent) {
        Integer ret = null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("fleetmanager")) {
                if (ret != null) {
                    throw new RuntimeException("more than one fleetmanager found in event");
                }
                ret = (Integer) ref.getId();
            }
        }
        return ret;
    }

    private static HistoryJsonClasses.TimeEntry[] readHistoryEntries(File name) throws IOException {
        // it creates a file with the specified name in the history directory

        // itreads the specified content from the  file
        FileReader reader = new FileReader(name);
        HistoryJsonClasses.TimeEntry[] timeentries = gson.fromJson(reader, HistoryJsonClasses.TimeEntry[].class);
        reader.close();
        return timeentries;
    }

    private void WriteGeneraldata(GlobalStationDataForExecution stationdat,
            GlobalUserDataForExecution userdat,
            GlobalManagerDataForExecution managerdat
    ) throws IOException {
        File outfile = this.analysispath.resolve("global_values.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        //write empty line
        String[] desc = new String[3];
        desc[0] = "simulation time (min)";
        desc[1] = Double.toString((double) totalsimtimespecified / 60D);
        desc[2] = "the data correspond to values up to this time (users, changes and any events after that time are ignored)";
        csvWriter.writeNext(desc);

        //first line statation data
        int maxrentfails;
        if (userdat.usertakefails.isEmpty()) {
            maxrentfails = 0;
        } else {
            maxrentfails = userdat.usertakefails.lastKey();
        }
        int maxreturnfails;
        if (userdat.userreturnfails.isEmpty()) {
            maxreturnfails = 0;
        } else {
            maxreturnfails = userdat.userreturnfails.lastKey();
        }

        //write empty line
        csvWriter.writeNext(new String[]{""});

        //Now set the String array for writing
        String[] record = new String[14 + maxrentfails + maxreturnfails + 2];

        //write header for user data
        record[0] = "#users total";
        record[1] = "#users finished in simulationtime";
        record[2] = "#abandoned";
        record[3] = "DS";
        record[4] = "HE";
        record[5] = "RE";
        record[6] = "Av. time to station (min)(only succesfull users)";
        record[7] = "Av. time from orig to dest station (min)(only succesfull users)";
        record[8] = "Av. time to final destination (min)(only succesfull users)";
        record[9] = "Av. total time (min)";
        record[10] = "Av. waitingtime in the total time (min)";
        record[11] = "Av. timeloss (min)";
        record[12] = "#failed rentals (only succesfull users)";
        record[13] = "#failed returns (only succesfull users)";
        int i = 0;
        while (i <= maxrentfails) {
            record[14 + i] = "# with " + i + " rental fails";
            i++;
        }
        int j = 0;
        while (j <= maxreturnfails) {
            record[14 + i + j] = "# with " + j + " return fails";
            j++;
        }
        csvWriter.writeNext(record);

        //write global user data
        record[0] = Integer.toString(userdat.totalusers);
        record[1] = Integer.toString(userdat.finishedusers);
        record[2] = Integer.toString(userdat.avabandonos);
        record[3] = Double.toString(userdat.DS);
        record[4] = Double.toString(userdat.HE);
        record[5] = Double.toString(userdat.RE);
        record[6] = Double.toString(userdat.avtostationtime);
        record[7] = Double.toString(userdat.avbetweenstationtime);
        record[8] = Double.toString(userdat.avfromstationtime);
        record[9] = Double.toString(userdat.avtostationtime + userdat.avbetweenstationtime + userdat.avfromstationtime);
        record[10] = Double.toString(userdat.avwaitingtime);
        record[11] = Double.toString(userdat.avtimeloss);
        record[12] = Integer.toString(userdat.totalfailedrentals);
        record[13] = Integer.toString(userdat.totalfailedreturns);
        for (Integer key : userdat.usertakefails.keySet()) {
            record[14 + key] = Integer.toString(userdat.usertakefails.get(key));
        }
        for (Integer key : userdat.userreturnfails.keySet()) {
            record[14 + maxrentfails + 1 + key] = Integer.toString(userdat.userreturnfails.get(key));
        }
        csvWriter.writeNext(record);

        //write empty line
        csvWriter.writeNext(new String[]{""});

        //now write sation summary
        record = new String[4];
        record[0] = "#stations";
        record[1] = "#stations with empty times";
        record[2] = "Av. empty times (min)";
        record[3] = "Av. equilibrium desviation (over stations and simulationtime)";
        csvWriter.writeNext(record);

        record[0] = Integer.toString(stationdat.totalstations);
        record[1] = Integer.toString(stationdat.numstationwithemtytimes);
        record[2] = Double.toString(stationdat.totalemptytimes);
        record[3] = Double.toString(stationdat.totaldeviationfromequilibrium);
        csvWriter.writeNext(record);

        //write empty line
        csvWriter.writeNext(new String[]{""});

        //now write manager summary
        record = new String[1 + (3 * managerdat.manager_event_data.keySet().size())];
        record[0] = "#total managing events";
        i = 1;
        for (String ev : managerdat.manager_event_data.keySet()) {
            record[i] = ev + " #all";
            record[i + 1] = ev + " #success";
            record[i + 2] = ev + " #failed";
            i = i + 3;
        }
        csvWriter.writeNext(record);

        record[0] = Integer.toString(managerdat.totalevents);
        i = 1;
        for (String ev : managerdat.manager_event_data.keySet()) {
            int[] dat = managerdat.manager_event_data.get(ev);
            record[i] = Integer.toString(dat[0]);
            record[i + 1] = Integer.toString(dat[1]);
            record[i + 2] = Integer.toString(dat[2]);
            i = i + 3;
        }
        csvWriter.writeNext(record);
        writer.close();
    }

}
