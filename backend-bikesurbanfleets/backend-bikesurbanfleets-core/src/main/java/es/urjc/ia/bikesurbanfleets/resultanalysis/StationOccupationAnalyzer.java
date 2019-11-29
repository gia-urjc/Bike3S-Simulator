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
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class StationOccupationAnalyzer {

    //the structure for storing stationdata
    //key is stationid
    //value is {stationid,availablebikes,capacity}
    private TreeMap<Integer, StationDataAnalyzer.StationMetric> stationmetrics;
    private TreeMap<Integer, int[]> stationoccupation = new TreeMap<Integer, int[]>();

    private int nexttimecheck;
    private boolean first=true;

    StationOccupationAnalyzer(TreeMap<Integer, StationDataAnalyzer.StationMetric> stations) {
        if (GlobalConfigurationParameters.STATION_OCCUPATION_CHECK_INTERVAL <= 0) {
            throw new RuntimeException("invalid values");
        }
        stationmetrics = stations;
        nexttimecheck = 0;
        first=true;
    }

    void postProcessAndWrite(Path outpath, int totalsimtimespecified) throws Exception {
        postProcessData(totalsimtimespecified);
        writeData(outpath, totalsimtimespecified);
    }

    private void postProcessData(int totalsimtimespecified) throws Exception {
        while (totalsimtimespecified > nexttimecheck) {
            int[] timeoccupation = new int[stationmetrics.keySet().size()];
            int i = 0;
            for (Integer sd : stationmetrics.keySet()) {
                timeoccupation[i] = stationmetrics.get(sd).currentavbikes;
                i++;
            }
            stationoccupation.put(nexttimecheck, timeoccupation);
            nexttimecheck = nexttimecheck + GlobalConfigurationParameters.STATION_OCCUPATION_CHECK_INTERVAL;
        }
        //finally add the entry at totalsimtimespecified 
        int[] timeoccupation = new int[stationmetrics.keySet().size()];
        int i = 0;
        for (Integer sd : stationmetrics.keySet()) {
            timeoccupation[i] = stationmetrics.get(sd).currentavbikes;
            i++;
        }
        stationoccupation.put(totalsimtimespecified, timeoccupation);
    }

    private void writeData(Path outpath, int totalsimtimespecified) throws IOException {
        File outfile = outpath.toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        String[] desc = new String[3];
        desc[0] = "simulation time (min)";
        desc[1] = Double.toString((double) totalsimtimespecified / 60D);
        desc[2] = "only events that occure within the simulation time are considered (changes after that time are ignored)";
        csvWriter.writeNext(desc);

        //Now set the String array for writing
        int numstations = stationmetrics.keySet().size();
        String[] record = new String[1 + numstations];
        String[] record2 = new String[1 + numstations];

        //write header and capacity
        record[0] = "StationId";
        record2[0] = "Capacity";
        int i = 1;
        for (Integer sid : stationmetrics.keySet()) {
            record[i] = Integer.toString(stationmetrics.get(sid).id);
            record2[i] = Integer.toString(stationmetrics.get(sid).capacity);
            i++;
        }
        csvWriter.writeNext(record);
        csvWriter.writeNext(record2);

        //write availabe bikes header
        String[] record3 = {"#available bikes per station at time (in seconds) from start"};
        csvWriter.writeNext(record3);

        //now write the availabler bikes
        for (int time : stationoccupation.keySet()) {
            int[] occupation = stationoccupation.get(time);
            if (occupation.length != numstations) {
                throw new RuntimeException("something wrong in station occupation calculation");
            }
            record[0] = Integer.toString( time);
            //now write the values for this time
            for (int j = 0; j < numstations; j++) {
                record[j + 1] = Integer.toString(occupation[j]);
            }
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    void analyzeEventEntryInTime(HistoryJsonClasses.EventEntry ee, int time) {
        while (time > nexttimecheck) {

            int[] timeoccupation = new int[stationmetrics.keySet().size()];
            int i = 0;
            for (Integer sd : stationmetrics.keySet()) {
                timeoccupation[i] = stationmetrics.get(sd).currentavbikes;
                i++;
            }
            stationoccupation.put(nexttimecheck, timeoccupation);
            if (first) {
                nexttimecheck+=120;
                first=false;
            } else {
            nexttimecheck = nexttimecheck + GlobalConfigurationParameters.STATION_OCCUPATION_CHECK_INTERVAL;
            }
        }
    }

}
