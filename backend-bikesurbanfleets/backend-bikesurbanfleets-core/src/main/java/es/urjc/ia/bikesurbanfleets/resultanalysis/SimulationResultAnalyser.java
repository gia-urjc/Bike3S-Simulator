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
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphHopperIntegration;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses.FinalGlobalValues;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricUser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class SimulationResultAnalyser {

    
    double standardstraightLineWalkingVelocity = 1.4 / GeoPoint.STRAIGT_LINE_FACTOR;
    double standardstraightLineCyclingVelocity = 6 / GeoPoint.STRAIGT_LINE_FACTOR;

    public static void main(String[] args) throws Exception {
        String historydir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/history/2/";
        String analysisdir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/analysis/2/";
        String tempdir = System.getProperty("user.home") + "/.Bike3S";
        String mapdir = "/Users/holger/workspace/BikeProjects/Bike3S/madrid.osm";

        GraphManager gm = new GraphHopperIntegration(mapdir, tempdir);
        SimulationResultAnalyser sa = new SimulationResultAnalyser(analysisdir, historydir, gm);
        sa.analyzeSimulation();
    }
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    private class UserMetric {
        int timeapp = -1;
        int timegetbike = -1;
        int timeretbike = -1;
        int timeleafe = -1;
        Event.RESULT_TYPE leafreason;
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
        double artificialtime = 0;
        GeoPoint lastposition;
    }

    private class ManagerEventMetric {

        int managerID = 0;
        String eventname = "";
        Event.RESULT_TYPE result;
        int time;
        int stationid;
    }

    private class StationMetric {

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
    TreeMap<Integer, StationMetric> stationmetrics = new TreeMap<Integer, StationMetric>();
    TreeMap<Integer, UserMetric> usermetrics = new TreeMap<Integer, UserMetric>();
    LinkedList<ManagerEventMetric> managereventsmetric = new LinkedList<ManagerEventMetric>();
    

    private Path analysispath;
    private Path historypath;
    private int totalsimtimespecified = 0;
    
    GraphManager routeService;

    public SimulationResultAnalyser(String analysisdir, String historydir, GraphManager routeService) throws IOException {
        this.analysispath = Paths.get(analysisdir);
        this.historypath = Paths.get(historydir);
        this.routeService = routeService;
    }

    public SimulationResultAnalyser(String analysisdir, String historydir) throws IOException {
        this.analysispath = Paths.get(analysisdir);
        this.historypath = Paths.get(historydir);
        this.routeService = null;
    }

    public void analyzeSimulation() throws Exception {
        // read global values
        File json = historypath.resolve("final-global-values.json").toFile();
        FileReader red = new FileReader(json);
        FinalGlobalValues fgv = gson.fromJson(red, FinalGlobalValues.class);
        totalsimtimespecified = fgv.getTotalTimeSimulation();
        red.close();

        // setup metrics
        stationmetrics = new TreeMap<Integer, StationMetric>();
        usermetrics = new TreeMap<Integer, UserMetric>();

        //preprocess data (read initial station info
        preprocessGetStationData();

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
        
        //postprocess user data and station data
        // changes some parts in the individual user and station data
        postProcessStationData();
        postProcessUserData();

        
        //write the results to files
        File auxiliaryDir = analysispath.toFile();
        if (!auxiliaryDir.exists()) {
            auxiliaryDir.mkdirs();
        }
        WriteUserdata();
        WriteStationdata();
        WriteManagerdata();
        
        //now generate global information for a summary file
        GlobalStationDataForExecution stationdata=getGlobalStationData();
        GlobalUserDataForExecution userdata=getGlobalUserData();
        GlobalManagerDataForExecution managerdata=getGlobalManagerData();
       
        WriteGeneraldata(stationdata, userdata, managerdata);
        
        //analyse station ocupation
        StationOccupationAnalysis soa=new StationOccupationAnalysis(this.analysispath, this.historypath);
        soa.analyzeStationOccupation();

    }

    private Integer getUserID(Collection<HistoryJsonClasses.IdReference> ent) {
        Integer ret=null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("users")) {
                if (ret!=null) throw new RuntimeException("more than one user found in event");
                ret= (Integer) ref.getId();
            }
        }
        if (ret!=null) return ret;
        throw new RuntimeException("no user id found");
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

    private Integer getManagerID(Collection<HistoryJsonClasses.IdReference> ent) {
        Integer ret=null;
        for (HistoryJsonClasses.IdReference ref : ent) {
            if (ref.getType().equals("fleetmanager")) {
                if (ret!=null) throw new RuntimeException("more than one fleetmanager found in event");
                ret= (Integer) ref.getId();
            }
        }
        if (ret!=null) return ret;
        throw new RuntimeException("no fleetmanager id found");
    }

    private void processHistoryEntries(HistoryJsonClasses.TimeEntry[] historyentries) {

        //find the entries
        for (HistoryJsonClasses.TimeEntry historyentry : historyentries) {
            int time = historyentry.getTime();

            for (HistoryJsonClasses.EventEntry ee : historyentry.getEvents()) {
                Event.EVENT_TYPE type = ee.getEventType();
                Event.RESULT_TYPE result = ee.getResult();
                String name = ee.getName();
                Collection<HistoryJsonClasses.IdReference> involvedEntities = ee.getInvolvedEntities();
                Integer stationid = getStationID(involvedEntities); //can have a station
                if (type == Event.EVENT_TYPE.USER_EVENT) {
                    Integer userid = getUserID(involvedEntities); //must have a user involved
                    if (userid == null) {
                        throw new RuntimeException("event must have a user");
                    }
                    analyzeUserData(ee.getNewEntities(), name, userid, stationid, time, result);
                } else if (type == Event.EVENT_TYPE.MANAGER_EVENT) {
                    Integer managerid = getManagerID(involvedEntities); //must have a user involved
                    if (managerid == null) {
                        throw new RuntimeException("event must have a manager");
                    }
                    analyzeManagerData(ee.getNewEntities(), name, managerid, stationid, time, result);
                }
                //station changes are inly considered up to totalsimulationtime 
                // for any tpe of event check changes in stations
                if (time <= totalsimtimespecified && stationid != null) {
                    StationMetric sm = stationmetrics.get(stationid);
                    checkChangesAvBikes(ee.getChanges(), time, sm);
                    analyzeStationData(name, sm, result);
                }
            }
        }
    }

    private void analyzeUserData(Map<String, List<JsonObject>> newEntities, String name, Integer userid, Integer stationId, int time, Event.RESULT_TYPE result) {
        //check new user appearance first
        if (name.equals("EventUserAppears")) {
            UserMetric newUM = new UserMetric();
            if (null != usermetrics.put(userid, newUM)) {
                throw new RuntimeException("user should not exist in historyanalysis");
            }
            if (newEntities == null) {
                throw new RuntimeException("user not found in new entities");
            }
            List<JsonObject> users = newEntities.get("users");
            if (users == null) {
                throw new RuntimeException("user not found in new entities");
            }
            HistoricUser hu = gson.fromJson(users.get(0), HistoricUser.class);
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
            return;
        }

        // in any other case the user should exist
        UserMetric usM = usermetrics.get(userid);
        StationMetric stM = null;
        if (stationId != null) {
            stM = stationmetrics.get(stationId);
        }
        switch (name) {
            case "EventUserArrivesAtStationToRentBike":
                usM.artificialtime += (usM.lastposition.distanceTo(stM.position) / standardstraightLineWalkingVelocity);
                usM.lastposition = stM.position;
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RENTAL) {
                    usM.succbikerentals++;
                    usM.timegetbike = time;
                    //user rents bike
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RENTAL) {
                    usM.failedbikerentals++;
                }
                break;
            case "EventUserArrivesAtStationToReturnBike":
                usM.artificialtime += (usM.lastposition.distanceTo(stM.position) / standardstraightLineCyclingVelocity);
                usM.lastposition = stM.position;
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RETURN) {
                    usM.succbikereturns++;
                    usM.timeretbike = time;
                    //user returns bike
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RETURN) {
                    usM.failedbaikereturns++;
                }
                break;
            case "EventUserTriesToReserveSlot":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_SLOT_RESERVATION) {
                    usM.succslotreservations++;
                } else if (result == Event.RESULT_TYPE.FAILED_SLOT_RESERVATION) {
                    usM.failesslotreservations++;
                }
                break;
            case "EventUserTriesToReserveBike":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RESERVATION) {
                    usM.succbikereservations++;
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RESERVATION) {
                    usM.failedbikereservations++;
                }
                break;
            case "EventUserLeavesSystem":
                usM.artificialtime += (usM.lastposition.distanceTo(usM.destination) / standardstraightLineWalkingVelocity);
                usM.lastposition = usM.destination;
                usM.leafreason = result;
                if (time <= totalsimtimespecified) {
                    usM.finishedinsimtime = true;
                }
                //user leafs
                usM.timeleafe = time;
                break;
            default:
                break;
        }

    }

    private void analyzeManagerData(Map<String, List<JsonObject>> newEntities, String name, Integer managerid, Integer stationId, int time, Event.RESULT_TYPE result) {
        //simply add the event
        ManagerEventMetric newMEM = new ManagerEventMetric();
        newMEM.eventname = name;
        newMEM.managerID = managerid;
        newMEM.time = time;
        newMEM.result = result;
        if (stationId != null) {
            newMEM.stationid = stationId;
        } else {
            newMEM.stationid = -1;
        }
        managereventsmetric.add(newMEM);
    }

    private void analyzeStationData(String name, StationMetric stM, Event.RESULT_TYPE result) {

        switch (name) {
            case "EventUserArrivesAtStationToRentBike":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RENTAL) {
                    stM.succbikerentals++;
                    //user rents bike
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RENTAL) {
                    stM.failedbikerentals++;
                }
                break;
            case "EventUserArrivesAtStationToReturnBike":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RETURN) {
                    stM.succbikereturns++;
                    //user returns bike
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RETURN) {
                    stM.failedbaikereturns++;
                }
                break;
            case "EventUserTriesToReserveSlot":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_SLOT_RESERVATION) {
                    stM.succslotreservations++;
                } else if (result == Event.RESULT_TYPE.FAILED_SLOT_RESERVATION) {
                    stM.failesslotreservations++;
                }
                break;
            case "EventUserTriesToReserveBike":
                if (result == Event.RESULT_TYPE.SUCCESSFUL_BIKE_RESERVATION) {
                    stM.succbikereservations++;
                } else if (result == Event.RESULT_TYPE.FAILED_BIKE_RESERVATION) {
                    stM.failedbikereservations++;
                }
                break;
            default:
                break;
        }
    }

    private void postProcessStationData()  {
        //add the rest times for the stations
       for (StationMetric sm : stationmetrics.values()) {
            if (sm.timelastchange > totalsimtimespecified) {
                throw new RuntimeException("some error");
            }
            double time = totalsimtimespecified - sm.timelastchange;
            if (sm.currentavbikes == 0) {
                sm.emtytime += time;
            }
            sm.balancingquality += Math.abs((double) sm.currentavbikes - ((double) sm.capacity / 2D)) * (double) time;
            sm.balancingquality = sm.balancingquality / totalsimtimespecified;
        }
    }
    private void postProcessUserData() throws Exception {
        for (UserMetric um : usermetrics.values()) {
            if (um.finishedinsimtime) {
                if (um.timegetbike >= 0) {//time counts if user did not abandom
                    addTimeComparison(um);
                }
            }
        }
    }

    private GlobalStationDataForExecution getGlobalStationData() throws GraphHopperIntegrationException, GeoRouteCreationException {
        GlobalStationDataForExecution dat=new GlobalStationDataForExecution();
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
   
    private GlobalUserDataForExecution getGlobalUserData() throws GraphHopperIntegrationException, GeoRouteCreationException {
        //globval values for users
        GlobalUserDataForExecution dat=new GlobalUserDataForExecution();
        dat.totalusers = usermetrics.size();
        dat.finishedusers  = 0;
        dat.avtostationtime  = 0;
        dat.avbetweenstationtime  = 0;
        dat.avfromstationtime  = 0;
        dat.usertakefails  = new TreeMap<>();
        dat.userreturnfails  = new TreeMap<>();
        dat.avtimeloss = 0;
        dat.totalfailedrentals = 0;
        dat.totalfailedreturns = 0;
        dat.avabandonos=0;
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
                if (um.leafreason != Event.RESULT_TYPE.EXIT_AFTER_REACHING_DESTINATION) {
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
        dat.DS = (double) succusers / (double) dat.finishedusers;
        dat.HE = (double) succusers / ((double) dat.totalfailedrentals + succusers);
        dat.RE = (double) succusers / ((double) dat.totalfailedreturns + succusers);

        return dat;
    }

    private GlobalManagerDataForExecution getGlobalManagerData() throws GraphHopperIntegrationException, GeoRouteCreationException {
//globval values for manager
        GlobalManagerDataForExecution dat=new GlobalManagerDataForExecution();
        dat.manager_event_data = new TreeMap<String, int[]>();
        dat.totalevents = managereventsmetric.size();
        for (ManagerEventMetric mem : managereventsmetric) {
            int[] d=dat.manager_event_data.get(mem.eventname);
            if (d==null) {
                int[] v ={0,0,0}; 
                d=v;
                dat.manager_event_data.put(mem.eventname,d);
            } 
            d[0]=d[0]+1;
            if (mem.result==Event.RESULT_TYPE.SUCCESS){
                d[1]=d[1]+1;
            } else if (mem.result==Event.RESULT_TYPE.FAIL){
                d[2]=d[2]+1;
            } else throw new RuntimeException("resulttype not allowed");
        }
        return dat;
    }

    private void preprocessGetStationData() throws IOException {
        File stfile = historypath.resolve("entities/stations.json").toFile();
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
        csvWriter.writeNext(new String[]{""});

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

        String[] record = new String[3];
        record[0] = "simulatiuon time (min)";
        record[1] = Double.toString((double) totalsimtimespecified / 60D);
        record[2] = "all global results are calculated up to this time (users not finishing up to this time are ignored)";
        csvWriter.writeNext(record);

        //write empty line
        csvWriter.writeNext(new String[]{""});

        //Now set the String array for writing
        record = new String[13 + maxrentfails + maxreturnfails + 2];

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
        record[10] = "Av. timeloss (min)";
        record[11] = "#failed rentals (only succesfull users)";
        record[12] = "#failed returns (only succesfull users)";
        int i = 0;
        while (i <= maxrentfails) {
            record[13 + i] = "# with " + i + " rental fails";
            i++;
        }
        int j = 0;
        while (j <= maxreturnfails) {
            record[13 + i + j] = "# with " + j + " return fails";
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
        record[10] = Double.toString(userdat.avtimeloss);
        record[11] = Integer.toString(userdat.totalfailedrentals);
        record[12] = Integer.toString(userdat.totalfailedreturns);
        for (Integer key : userdat.usertakefails.keySet()) {
            record[13 + key] = Integer.toString(userdat.usertakefails.get(key));
        }
        for (Integer key : userdat.userreturnfails.keySet()) {
            record[13 + maxrentfails + 1 + key] = Integer.toString(userdat.userreturnfails.get(key));
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
        record = new String[1+(3*managerdat.manager_event_data.keySet().size())];
        record[0] = "#total managing events";
        i=1;
        for (String ev: managerdat.manager_event_data.keySet()){
            record[i]=ev+" #all";
            record[i+1]=ev+" #success";
            record[i+2]=ev+" #failed";
            i=i+3;
        }
        csvWriter.writeNext(record);

        record[0] = Integer.toString(managerdat.totalevents);
        i=1;
        for (String ev: managerdat.manager_event_data.keySet()){
            int [] dat=managerdat.manager_event_data.get(ev);
            record[i]=Integer.toString(dat[0]);
            record[i+1]=Integer.toString(dat[1]);
            record[i+2]=Integer.toString(dat[2]);
            i=i+3;
        }
        csvWriter.writeNext(record);
        writer.close();
    }

    private void WriteUserdata() throws IOException {
        File outfile = this.analysispath.resolve("users.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        //Now set the String array for writing
        String[] record = new String[17];

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
            if (um.timegetbike >= 0) {
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
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    private void WriteManagerdata() throws IOException {
        File outfile = this.analysispath.resolve("fleetManager.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

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

    private void WriteStationdata() throws IOException {
        File outfile = this.analysispath.resolve("stations.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

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

    //compare the shortes routetime with the actual routetime 
    //the shortes rute is calculated by specifying the closest station to the user position, the clostes station to the user destination 
    // and searching the rutepartes inbetween 
    private void addTimeComparison(UserMetric um) throws Exception {

        double closeststartdist = Double.MAX_VALUE;
        double closestenddist = Double.MAX_VALUE;
        StationMetric bestStartStation = null;
        StationMetric bestEndStation = null;
        for (StationMetric hs : stationmetrics.values()) {
            double currentdist = hs.position.distanceTo(um.origin);
            if (currentdist < closeststartdist) {
                closeststartdist = currentdist;
                bestStartStation = hs;
            }
            currentdist = hs.position.distanceTo(um.destination);
            if (currentdist < closestenddist) {
                closestenddist = currentdist;
                bestEndStation = hs;
            }
        }
        //Now get the routes
        double shortesttime = 0;
        if (routeService != null) {
            GeoRoute gr = routeService.obtainShortestRouteBetween(um.origin, bestStartStation.position, "foot");
            shortesttime += (int) (gr.getTotalDistance() / um.walkvelocity);
            gr = routeService.obtainShortestRouteBetween(bestStartStation.position, bestEndStation.position, "bike");
            shortesttime += (int) (gr.getTotalDistance() / um.cyclevelocity);
            gr = routeService.obtainShortestRouteBetween(bestEndStation.position, um.destination, "foot");
            shortesttime += (int) (gr.getTotalDistance() / um.walkvelocity);
            um.additionaltimeloss = (um.timeleafe - um.timeapp) - shortesttime;
        } else {

            shortesttime = ((um.origin.distanceTo(bestStartStation.position) / this.standardstraightLineWalkingVelocity)
                    + (bestStartStation.position.distanceTo(bestEndStation.position) / this.standardstraightLineCyclingVelocity)
                    + (bestEndStation.position.distanceTo(um.destination) / this.standardstraightLineWalkingVelocity));
            um.additionaltimeloss = (int) (um.artificialtime - shortesttime);
        }
    }

}
