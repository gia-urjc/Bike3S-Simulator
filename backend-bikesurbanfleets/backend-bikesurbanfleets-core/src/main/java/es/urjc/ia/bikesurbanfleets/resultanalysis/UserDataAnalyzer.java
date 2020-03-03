/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.resultanalysis;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.core.UserEvents.EventUser;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricUser;
import es.urjc.ia.bikesurbanfleets.resultanalysis.StationDataAnalyzer.StationMetric;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;

/**
 *
 * @author holger
 */
class UserDataAnalyzer {

    private GraphManager routeService;

    private static class UserMetric {

        int timelastevent = 0;
        int timeapp = -1;
        int timegetbike = -1;
        int timeretbike = -1;
        int timeleafe = -1;
        int waitingtime = 0;
        EventUser.EXIT_REASON leafreason = null;
        int succbikereservations = 0;
        int failedbikereservations = 0;
        int succslotreservations = 0;
        int failesslotreservations = 0;
        int succbikerentals = 0;
        int failedbikerentals = 0;
        int succbikereturns = 0;
        int failedbaikereturns = 0;
        boolean finishedinsimtime = false;
        GeoPoint origin;
        GeoPoint destination;
        double walkvelocity;
        double cyclevelocity;
        double additionaltimeloss = 0;
        GeoPoint lastposition;
    }

    private TreeMap<Integer, UserMetric> usermetrics;
    private TreeMap<Integer, StationMetric> stationmetrics;

    UserDataAnalyzer(TreeMap<Integer, StationMetric> stations,
            GraphManager routeService) {
        usermetrics = new TreeMap<Integer, UserMetric>();
        this.routeService = routeService;
        stationmetrics = stations;
    }

    void postProcessAndWrite(Path outpath, int totalsimtimespecified) throws Exception {
        postProcessData();
        writeData(outpath, totalsimtimespecified);
    }

    private void postProcessData() throws Exception {
        for (UserMetric um : usermetrics.values()) {
            if (um.finishedinsimtime) {
                if (um.timegetbike >= 0) {//time counts if user did not abandom
                    addTimeComparison(um);
                }
            }
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
        desc[0] = "simulatiuon time (min)";
        desc[1] = Double.toString((double) totalsimtimespecified / 60D);
        desc[2] = "only events that occure within the simulation time are considered (users not finishing up to this time are ignored)";
        csvWriter.writeNext(desc);

        //Now set the String array for writing
        String[] record = new String[18];

        //write header
        record[0] = "id";
        record[1] = "finished in simulationtime";
        record[2] = "appearance time (sec form start)";
        record[3] = "get bike time (sec from start)";
        record[4] = "return bike time (sec from start)";
        record[5] = "at final destination (sec from start)";
        record[6] = "total time (min)";
        record[7] = "additional time used over best aproximate time (min)";
        record[8] = "exit reason";
        record[9] = "Successful bike reservations";
        record[10] = "Failed bike reservations";
        record[11] = "Successful slot reservations";
        record[12] = "Failed slot reservations";
        record[13] = "Successful bike rentals";
        record[14] = "Failed bike rentals";
        record[15] = "Successful bike returns";
        record[16] = "Failed bike returns";
        record[17] = "Time waited to retry";

        csvWriter.writeNext(record);

        //now write the user values
        for (Integer id : usermetrics.keySet()) {
            boolean aband = true;
            UserMetric um = usermetrics.get(id);
            record[0] = Integer.toString(id);
            record[2] = Integer.toString(um.timeapp);
            if (um.timegetbike >= 0) {
                aband = false;
            }
            if (um.finishedinsimtime) {
                record[1] = "yes";
            } else {
                record[1] = "no";
            }
            if (um.timegetbike >= 0) {
                record[3] = Integer.toString(um.timegetbike);
            } else {
                record[3] = "";
            }
            if (um.timeretbike >= 0) {
                record[4] = Integer.toString(um.timeretbike);
            } else {
                record[4] = "";
            }
            if (um.timeleafe >= 0) {
                record[5] = Integer.toString(um.timeleafe);
            } else {
                record[5] = "";
            }
            if (um.finishedinsimtime && !aband) {
                record[6] = Double.toString(((double) (um.timeleafe - um.timeapp)) / 60D);
                record[7] = Double.toString(((double) um.additionaltimeloss) / 60D);
            } else {
                record[6] = "";
                record[7] = "";
            }
            if (um.finishedinsimtime) {
                record[8] = um.leafreason.toString();
            } else {
                record[8] = "";
            }
            record[9] = Integer.toString(um.succbikereservations);
            record[10] = Integer.toString(um.failedbikereservations);
            record[11] = Integer.toString(um.succslotreservations);
            record[12] = Integer.toString(um.failesslotreservations);
            record[13] = Integer.toString(um.succbikerentals);
            record[14] = Integer.toString(um.failedbikerentals);
            record[15] = Integer.toString(um.succbikereturns);
            record[16] = Integer.toString(um.failedbaikereturns);
            record[17] = Integer.toString(um.waitingtime);
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    //compare the shortes routetime with the actual routetime 
    //the shortes rute is calculated by specifying the closest station to the user position, the clostes station to the user destination 
    // and searching the rutepartes inbetween 
    private void addTimeComparison(UserMetric um) throws Exception {

        double closeststartdist = Double.MAX_VALUE;
        double closestenddist = Double.MAX_VALUE;
        StationMetric bestStartStation = null;
        StationMetric bestEndStation = null;
        for (StationMetric hs : stationmetrics.values()) {
            double currentdist = routeService.estimateDistance(um.origin, hs.position, "foot");
            if (currentdist < closeststartdist) {
                closeststartdist = currentdist;
                bestStartStation = hs;
            }
            currentdist = routeService.estimateDistance(hs.position, um.destination, "foot");
            if (currentdist < closestenddist) {
                closestenddist = currentdist;
                bestEndStation = hs;
            }
        }
        //Now get the routes
        double shortesttime = 0;
        shortesttime += (int) (closeststartdist / um.walkvelocity);
        double bikedist = (int) routeService.estimateDistance(bestStartStation.position, bestEndStation.position, "bike");
        shortesttime += (int) (bikedist / um.cyclevelocity);
        shortesttime += (int) (closestenddist / um.walkvelocity);
        um.additionaltimeloss = (um.timeleafe - um.timeapp) - shortesttime;

    }

    void analyzeEventEntryInTime(HistoryJsonClasses.EventEntry ee, int time) {
        Event.EVENT_TYPE type = ee.getEventType();
        if (type != Event.EVENT_TYPE.USER_EVENT) {
            return;
        }

        //if user event
        Event.RESULT_TYPE result = ee.getResult();
        String name = ee.getName();
        Integer userid = SimulationResultAnalyser.getUserID(ee.getInvolvedEntities()); //must have a user involved
        if (userid == null) { //user events must have a user involved
            throw new RuntimeException("event must have a user");
        }

        //now analyze the event
        //check new user appearance first
        if (name.equals("EventUserAppears")) {
            UserMetric newUM = new UserMetric();
            if (null != usermetrics.put(userid, newUM)) {
                throw new RuntimeException("user should not exist in historyanalysis");
            }
            if (ee.getNewEntities() == null) {
                throw new RuntimeException("user not found in new entities");
            }
            List<JsonObject> users = ee.getNewEntities().get("users");
            if (users == null) {
                throw new RuntimeException("user not found in new entities");

            }
            HistoricUser hu = SimulationResultAnalyser.gson.fromJson(users.get(0), HistoricUser.class);
            if (userid != hu.getId()) {
                throw new RuntimeException("user not found in new entities");
            }

            //now copy the user data:
            newUM.origin = hu.getPosition();
            newUM.destination = hu.getDestinationLocation();
            newUM.walkvelocity = hu.getWalkingVelocity();
            newUM.cyclevelocity = hu.getCyclingVelocity();
            newUM.timeapp = time;
            newUM.lastposition = newUM.origin;
            newUM.timelastevent = time;
            return;
        }

        // in any other case the user should exist
        UserMetric usM = usermetrics.get(userid);
        Integer stationid = SimulationResultAnalyser.getStationID(ee.getInvolvedEntities());
        if (usM.timelastevent > time) {
            throw new RuntimeException("event can not be before last event");
        }
        StationMetric stM = null;
        if (stationid != null) {
            stM = stationmetrics.get(stationid);
        }
        switch (name) {
            case "EventUserArrivesAtStationToRentBike":
                usM.lastposition = stM.position;
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    usM.succbikerentals++;
                    usM.timegetbike = time;
                    //user rents bike
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    if (ee.getAdditionalInfo() == Event.ADDITIONAL_INFO.RETRY_EVENT) {
                        usM.waitingtime += time - usM.timelastevent;
                    } else {
                        usM.failedbikerentals++;
                    }
                }
                break;
            case "EventUserArrivesAtStationToReturnBike":
                usM.lastposition = stM.position;
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    usM.succbikereturns++;
                    usM.timeretbike = time;
                    //user returns bike
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    if (ee.getAdditionalInfo() == Event.ADDITIONAL_INFO.RETRY_EVENT) {
                        usM.waitingtime += time - usM.timelastevent;
                    } else {
                        usM.failedbaikereturns++;
                    }
                }
                break;
            case "EventUserTriesToReserveSlot":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    usM.succslotreservations++;
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    if (ee.getAdditionalInfo() == Event.ADDITIONAL_INFO.RETRY_EVENT) {
                        usM.waitingtime += time - usM.timelastevent;
                    } else {
                        usM.failesslotreservations++;
                    }
                }
                break;
            case "EventUserTriesToReserveBike":
                if (result == Event.RESULT_TYPE.SUCCESS) {
                    usM.succbikereservations++;
                } else if (result == Event.RESULT_TYPE.FAIL) {
                    if (ee.getAdditionalInfo() == Event.ADDITIONAL_INFO.RETRY_EVENT) {
                        usM.waitingtime += time - usM.timelastevent;
                    } else {
                        usM.failedbikereservations++;
                    }
                }
                break;
            case "EventUserLeavesSystem":
                usM.lastposition = usM.destination;
                usM.leafreason = EventUser.EXIT_REASON.valueOf(ee.getAdditionalInfo().name());
                usM.finishedinsimtime = true;
                //user leafs
                usM.timeleafe = time;
                break;
            default:
                break;
        }
        usM.timelastevent = time;
    }

    static class GlobalUserDataForExecution {

        double avtostationtime = 0;
        int totalusers = 0;
        int finishedusers = 0;
        double avbetweenstationtime = 0;
        double avfromstationtime = 0;
        int avabandonos = 0;
        double DS = 0.0D;
        double HE = 0.0D;
        double RE = 0.0D;
        TreeMap<Integer, Integer> usertakefails = new TreeMap<>();
        TreeMap<Integer, Integer> userreturnfails = new TreeMap<>();
        double avtimeloss = 0;
        int totalfailedrentals = 0;
        int totalfailedreturns = 0;
        double avwaitingtime = 0;
    }

    GlobalUserDataForExecution getGlobalUserData() {
        //globval values for users
        GlobalUserDataForExecution dat = new GlobalUserDataForExecution();
        dat.totalusers = usermetrics.size();
        dat.finishedusers = 0;
        dat.avtostationtime = 0;
        dat.avbetweenstationtime = 0;
        dat.avfromstationtime = 0;
        dat.usertakefails = new TreeMap<>();
        dat.userreturnfails = new TreeMap<>();
        dat.avtimeloss = 0;
        dat.totalfailedrentals = 0;
        dat.totalfailedreturns = 0;
        dat.avabandonos = 0;
        dat.avwaitingtime = 0;
        int succusers = 0;
        int i = 0;
        for (UserMetric um : usermetrics.values()) {
            if (um.finishedinsimtime) {
                dat.finishedusers++;
                if (um.timegetbike >= 0) {//time counts if user did not abandom
                    succusers++;
                    dat.avtostationtime += um.timegetbike - um.timeapp;
                    dat.avbetweenstationtime += um.timeretbike - um.timegetbike;
                    dat.avfromstationtime += um.timeleafe - um.timeretbike;
                    dat.totalfailedrentals += um.failedbikerentals;
                    dat.totalfailedreturns += um.failedbaikereturns;
                    dat.avwaitingtime += um.waitingtime;
                    Integer old = dat.usertakefails.get(um.failedbikerentals);
                    if (old == null) {
                        dat.usertakefails.put(um.failedbikerentals, 1);
                    } else {
                        dat.usertakefails.put(um.failedbikerentals, old + 1);
                    }
                    old = dat.userreturnfails.get(um.failedbaikereturns);
                    if (old == null) {
                        dat.userreturnfails.put(um.failedbaikereturns, 1);
                    } else {
                        dat.userreturnfails.put(um.failedbaikereturns, old + 1);
                    }
                    dat.avtimeloss += um.additionaltimeloss;
                }
                if (um.leafreason != EventUser.EXIT_REASON.EXIT_AFTER_REACHING_DESTINATION) {
                    dat.avabandonos++;
                }
            }
        }
        if (dat.finishedusers != succusers + dat.avabandonos) {
            throw new RuntimeException("something wrong in data analsysis");
        }
        dat.avtimeloss = dat.avtimeloss / ((double) succusers * 60D);
        dat.avtostationtime = dat.avtostationtime / ((double) succusers * 60D);
        dat.avbetweenstationtime = dat.avbetweenstationtime / ((double) succusers * 60D);
        dat.avfromstationtime = dat.avfromstationtime / ((double) succusers * 60D);
        dat.avwaitingtime = dat.avwaitingtime / ((double) succusers * 60D);
        dat.DS = (double) succusers / (double) dat.finishedusers;
        dat.HE = (double) succusers / ((double) dat.totalfailedrentals + succusers);
        dat.RE = (double) succusers / ((double) dat.totalfailedreturns + succusers);

        return dat;
    }

}
