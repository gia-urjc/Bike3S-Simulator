/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
import static es.urjc.ia.bikesurbanfleets.resultanalysis.SimulationResultAnalyser.gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
class StationDataAnalyzer {

    static class StationMetric {

        GeoPoint position;
        int id;
        int succbikereservations = 0;
        int failedbikereservations = 0;
        int succslotreservations = 0;
        int failesslotreservations = 0;
        int succbikerentals = 0;
        int failedbikerentals = 0;
        int succbikereturns = 0;
        int failedbaikereturns = 0;
        double balancingquality = 0D;
        long emtytime = 0;
        int timelastchange = 0;
        int currentavbikes = 0;
        int capacity;
    }
    private TreeMap<Integer, StationMetric> stationmetrics;

    StationDataAnalyzer() {
        stationmetrics = new TreeMap<Integer, StationMetric>();
    }

    TreeMap<Integer, StationMetric> getStationData() {
        return stationmetrics;
    }

    void readInitialStationData(Path input) throws IOException {
        File stfile = input.toFile();
        FileReader r = new FileReader(stfile);
        JsonObject jo = gson.fromJson(new FileReader(stfile), JsonObject.class);
        JsonArray st = jo.getAsJsonArray("instances");
        for (JsonElement je : st) {
            HistoricStation hs = gson.fromJson(je, HistoricStation.class);
            Integer id = hs.getId();
            StationMetric sm = new StationMetric();
            sm.capacity = hs.getCapacity();
            sm.currentavbikes = hs.getAvailablebikes();
            sm.position = hs.getPosition();
            sm.id = id;
            if (stationmetrics.put(id, sm) != null) {
                throw new RuntimeException("duplicate station");
            }
        }
        r.close();
    }

    void postProcessAndWrite(Path outpath, int totalsimtimespecified) throws Exception {
        postProcessData(totalsimtimespecified);
        writeData(outpath, totalsimtimespecified);
    }

    private void postProcessData(int totalsimtimespecified) {
        //add the rest times for the stations
        for (StationMetric sm : stationmetrics.values()) {
            if (sm.timelastchange > totalsimtimespecified) {
                throw new RuntimeException("some error");
            }
            double time = (totalsimtimespecified - 1) - sm.timelastchange;
            if (sm.currentavbikes == 0) {
                sm.emtytime += time;
            }
            sm.balancingquality += Math.abs((double) sm.currentavbikes - ((double) sm.capacity / 2D)) * (double) time;
            sm.balancingquality = sm.balancingquality / totalsimtimespecified;
        }
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
        String[] record = new String[11];

        //write header
        record[0] = "id";
        record[1] = "emptytime (min)";
        record[2] = "average deviation from halfcapacity";
        record[3] = "Successful bike reservations";
        record[4] = "Failed bike reservations";
        record[5] = "Successful slot reservations";
        record[6] = "Failed slot reservations";
        record[7] = "Successful bike rentals";
        record[8] = "Failed bike rentals";
        record[9] = "Successful bike returns";
        record[10] = "Failed bike returns";
        csvWriter.writeNext(record);

        //now write the station data
        for (Integer id : stationmetrics.keySet()) {
            StationMetric sm = stationmetrics.get(id);
            //postprocess information
            record[0] = Integer.toString(id);
            record[1] = Double.toString((double) sm.emtytime / 60D);
            record[2] = Double.toString(sm.balancingquality);
            record[3] = Integer.toString(sm.succbikereservations);
            record[4] = Integer.toString(sm.failedbikereservations);
            record[5] = Integer.toString(sm.succslotreservations);
            record[6] = Integer.toString(sm.failesslotreservations);
            record[7] = Integer.toString(sm.succbikerentals);
            record[8] = Integer.toString(sm.failedbikerentals);
            record[9] = Integer.toString(sm.succbikereturns);
            record[10] = Integer.toString(sm.failedbaikereturns);
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    void analyzeEventEntryInTime(HistoryJsonClasses.EventEntry ee, int time) {
        Event.RESULT_TYPE result = ee.getResult();
        String name = ee.getName();
        Integer stationid = SimulationResultAnalyser.getStationID(ee.getInvolvedEntities()); //can have a station
        //station changes are inly considered up to totalsimulationtime 
        // for any tpe of event check changes in stations
        if (stationid != null) {
            StationMetric sm = stationmetrics.get(stationid);
            checkChangesAvBikes(ee.getChanges(), time, sm);
            analyzeStationData(name, sm, result);
        }
    }

    private void analyzeStationData(String name, StationMetric stM, Event.RESULT_TYPE result) {
        switch (name) {
            case "EventUserArrivesAtStationToRentBike":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    stM.succbikerentals++;
                    //user rents bike
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    stM.failedbikerentals++;
                }
                break;
            case "EventUserArrivesAtStationToReturnBike":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    stM.succbikereturns++;
                    //user returns bike
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    stM.failedbaikereturns++;
                }
                break;
            case "EventUserTriesToReserveSlot":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    stM.succslotreservations++;
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    stM.failesslotreservations++;
                }
                break;
            case "EventUserTriesToReserveBike":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    stM.succbikereservations++;
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    stM.failedbikereservations++;
                }
                break;
            default:
                break;
        }
    }

    private void checkChangesAvBikes(Map<String, List<JsonObject>> changes, int currenttime, StationMetric sm) {
        if (changes == null) {
            return;
        }
        List<JsonObject> stationch = changes.get("stations");
        if (stationch == null) {
            return;
        }
        for (JsonObject o : stationch) {
            int id = o.get("id").getAsInt();
            if (sm.id != id) {
                throw new RuntimeException("station not in involved entities but in changes");
            }
            JsonObject o1 = o.getAsJsonObject("availablebikes");
            if (o1 != null) {
                int oldv = o1.get("old").getAsInt();
                int newv = o1.get("new").getAsInt();
                if (sm.currentavbikes != oldv) {
                    throw new RuntimeException("invalid value");
                }
                double time = currenttime - sm.timelastchange;
                if (sm.currentavbikes == 0) {
                    sm.emtytime += time;
                }
                sm.balancingquality += Math.abs((double) sm.currentavbikes - ((double) sm.capacity / 2D)) * (double) time;
                sm.currentavbikes = newv;
                sm.timelastchange = currenttime;
            }
        }
    }

    static class GlobalStationDataForExecution {

        int numstationwithemtytimes = 0;
        double totalemptytimes = 0;
        int totalstations = 0;
        double totaldeviationfromequilibrium = 0;
    }

    GlobalStationDataForExecution getGlobalStationData() {
        GlobalStationDataForExecution dat = new GlobalStationDataForExecution();
        dat.numstationwithemtytimes = 0;
        dat.totalemptytimes = 0;
        dat.totaldeviationfromequilibrium = 0;
        dat.totalstations = stationmetrics.size();
        for (StationMetric sm : stationmetrics.values()) {
            if (sm.emtytime > 0) {
                dat.numstationwithemtytimes++;
            }
            dat.totalemptytimes += sm.emtytime;
            dat.totaldeviationfromequilibrium += sm.balancingquality;
        }
        dat.totalemptytimes = dat.totalemptytimes / (60D * (double) dat.totalstations);
        dat.totaldeviationfromequilibrium = dat.totaldeviationfromequilibrium / (double) dat.totalstations;
        return dat;
    }

}
