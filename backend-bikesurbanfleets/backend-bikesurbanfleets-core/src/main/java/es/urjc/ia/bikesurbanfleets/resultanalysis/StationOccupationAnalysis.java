/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class StationOccupationAnalysis  {

    //for checking the station ocuppation 
    final int stationOccupationCheckInterval=60*60; //every 1 hour
    
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    //the structure for storing stationdata
    //key is stationid
    //value is {stationid,availablebikes,capacity}
    TreeMap<Integer, int[]> stationtree = new TreeMap<Integer, int[]>();
    TreeMap<Integer, TreeMap<Integer, Integer>> stationoccupation = new TreeMap<Integer,TreeMap<Integer, Integer>>();
    

    private Path analysispath;
    private Path historypath;

    public StationOccupationAnalysis(Path analysisdir, Path historydir) {
        this.analysispath = analysisdir;
        this.historypath = historydir;
    }

    public void analyzeStationOccupation() throws Exception {
        readInitialStationData();

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
        
        //write the results to files
        File auxiliaryDir = analysispath.toFile();
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        writeData();
    }

    private Integer getStationID(Collection<HistoryJsonClasses.IdReference> ent) {
       Integer ret=null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("stations")) {
                if (ret!=null) throw new RuntimeException("more than one station found in event");
                ret= (Integer) ref.getId();
            }
        }
        return ret;
    }

    private void processHistoryEntries(HistoryJsonClasses.TimeEntry[] historyentries) {

        int nexttimecheck=0;
        //find the entries
        for (HistoryJsonClasses.TimeEntry historyentry : historyentries) {
            int time = historyentry.getTime();

            if (time>nexttimecheck) {
                addStationValues(nexttimecheck);
                nexttimecheck=nexttimecheck+stationOccupationCheckInterval;
            }
            for (HistoryJsonClasses.EventEntry ee : historyentry.getEvents()) {
                Collection<HistoryJsonClasses.IdReference> involvedEntities = ee.getInvolvedEntities();
                Integer stationid = getStationID(involvedEntities); //can have a station
                checkChangesAvBikes(ee.getChanges(), time, stationid);
            }
        }
    }

    private void addStationValues(int time){
        TreeMap<Integer, Integer> currentstationtree=new TreeMap<Integer, Integer>();
        int i=0;
        for (int[] sd:stationtree.values()){
            currentstationtree.put(sd[0], sd[1]);
        }
        stationoccupation.put(time, currentstationtree);
    }
            
    private void readInitialStationData() throws IOException {
        File stfile = historypath.resolve("entities/stations.json").toFile();
        FileReader r = new FileReader(stfile);
        JsonObject jo = gson.fromJson(new FileReader(stfile), JsonObject.class);
        JsonArray st = jo.getAsJsonArray("instances");
        for (JsonElement je : st) {
            HistoricStation hs = gson.fromJson(je, HistoricStation.class);
            Integer id = hs.getId();
            int[] sd = new int[3];
            sd[0] = id;
            sd[1] = hs.getAvailablebikes();
            sd[2] = hs.getCapacity();
            if (stationtree.put(id, sd) != null) {
                throw new RuntimeException("duplicate station");
            }
        }
        r.close();
    }

    private void checkChangesAvBikes(Map<String, List<JsonObject>> changes, int currenttime, Integer stationID) {
        if (changes == null) {
            return;
        }
        List<JsonObject> stationch = changes.get("stations");
        if (stationch == null) {
            return;
        }
        for (JsonObject o : stationch) {
            int id = o.get("id").getAsInt();
            if (stationID==null || stationID.intValue() != id) {
                throw new RuntimeException("station not in involved entities but in changes");
            }
            JsonObject o1 = o.getAsJsonObject("availablebikes");
            if (o1 != null) {
                int[] sd=stationtree.get(stationID);
                int oldv = o1.get("old").getAsInt();
                int newv = o1.get("new").getAsInt();
                if (sd[1] != oldv) {
                    throw new RuntimeException("invalid value");
                }
                sd[1] = newv;
            }
        }
    }

    private static HistoryJsonClasses.TimeEntry[] readHistoryEntries(File name) throws IOException {
        // it creates a file with the specified name in the history directory

        // itreads the specified content from the  file
        FileReader reader = new FileReader(name);
        HistoryJsonClasses.TimeEntry[] timeentries = gson.fromJson(reader, HistoryJsonClasses.TimeEntry[].class);
        reader.close();
        return timeentries;
    }


    private void writeData() throws IOException {
        File outfile = this.analysispath.resolve("stationocupation.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        //Now set the String array for writing
        String[] record = new String[1+stationtree.size()];

        //write header
        record[0] = "StationId";
        int i=1;
        for (int[] sd:stationtree.values()){
            record[i]=Integer.toString(sd[0]);
            i++;
        }
        csvWriter.writeNext(record);
        
        //write capacity
        record[0] = "Capacity";
        i=1;
        for (int[] sd:stationtree.values()){
            record[i]=Integer.toString(sd[2]);
            i++;
        }
        csvWriter.writeNext(record);
        //write availabe bikes header
        String [] record2 = {"#available bikes at time from start"};
        csvWriter.writeNext(record2);
        
        //now write the availabler bikes
        for (int time:stationoccupation.keySet()){
            TreeMap<Integer, Integer> currentstationtree= stationoccupation.get(time);
            record[0] = Integer.toString(time/60);
            i=1;
            for (int val:currentstationtree.values()){
                record[i]=Integer.toString(val);
                i++;
            }
            csvWriter.writeNext(record);
        }
        writer.close();
    }
}
