/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class ManagerDataAnalyzer {

    static private class ManagerEventMetric {

        int managerID = 0;
        String eventname = "";
        Event.RESULT_TYPE result;
        int time;
        int stationid;
    }

    private LinkedList<ManagerEventMetric> managereventsmetric;

    ManagerDataAnalyzer() {
        managereventsmetric = new LinkedList<ManagerEventMetric>();
    }

    void postProcessAndWrite(Path outpath, int totalsimtimespecified) throws Exception {
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
        desc[2] = "only events that occure within the simulation time are considered (events after that time are ignored)";
        csvWriter.writeNext(desc);

        //Now set the String array for writing
        String[] record = new String[5];

        //write header
        record[0] = "manager id";
        record[1] = "event";
        record[2] = "time";
        record[3] = "result";
        record[4] = "involved station";
        csvWriter.writeNext(record);

        //now write the user values
        for (ManagerEventMetric mem : managereventsmetric) {
            record[0] = Integer.toString(mem.managerID);
            record[1] = mem.eventname;
            record[2] = Integer.toString(mem.time);
            record[3] = mem.result.toString();
            if (mem.stationid >= 0) {
                record[4] = Integer.toString(mem.stationid);
            } else {
                record[4] = "";
            }
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    void analyzeEventEntryInTime(HistoryJsonClasses.EventEntry ee, int time) {
        Event.EVENT_TYPE type = ee.getEventType();
        if (type != Event.EVENT_TYPE.MANAGER_EVENT) {
            return;
        }

        //if manager event
        Event.RESULT_TYPE result = ee.getResult();
        String name = ee.getName();
        Integer managerid = SimulationResultAnalyser.getManagerID(ee.getInvolvedEntities()); //must have a user involved
        if (managerid == null) { //manager events must have a managerid involved
            throw new RuntimeException("event must have a manager");
        }
        Integer stationid = SimulationResultAnalyser.getStationID(ee.getInvolvedEntities());

        //simply add the event
        ManagerEventMetric newMEM = new ManagerEventMetric();
        newMEM.eventname = name;
        newMEM.managerID = managerid;
        newMEM.time = time;
        newMEM.result = result;
        if (stationid != null) {
            newMEM.stationid = stationid;
        } else {
            newMEM.stationid = -1;
        }
        managereventsmetric.add(newMEM);

    }

    static class GlobalManagerDataForExecution {
        TreeMap<String, int[]> manager_event_data = new TreeMap<String, int[]>();
        int totalevents = 0;
    }

    GlobalManagerDataForExecution getGlobalManagerData() {
//globval values for manager
        GlobalManagerDataForExecution dat = new GlobalManagerDataForExecution();
        dat.manager_event_data = new TreeMap<String, int[]>();
        dat.totalevents = managereventsmetric.size();
        for (ManagerEventMetric mem : managereventsmetric) {
            int[] d = dat.manager_event_data.get(mem.eventname);
            if (d == null) {
                int[] v = {0, 0, 0};
                d = v;
                dat.manager_event_data.put(mem.eventname, d);
            }
            d[0] = d[0] + 1;
            if (mem.result == Event.RESULT_TYPE.SUCCESS) {
                d[1] = d[1] + 1;
            } else if (mem.result == Event.RESULT_TYPE.FAIL) {
                d[2] = d[2] + 1;
            } else {
                throw new RuntimeException("resulttype not allowed");
            }
        }
        return dat;
    }

}
