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
import es.urjc.ia.bikesurbanfleets.core.CompareTestApplication;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses;
import es.urjc.ia.bikesurbanfleets.history.HistoryJsonClasses.FinalGlobalValues;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricStation;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author holger
 */
public class SimulationResultAnalyser {
    
    public static void main(String[] args) throws Exception {
        String historydir= "/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/history/2/";
         String analysisdir="/Users/holger/workspace/BikeProjects/Bike3S/Bike3STests/tests600max/fasttest/1/analysis/2/";
        SimulationResultAnalyser sa=new SimulationResultAnalyser(analysisdir, historydir);
        sa.analyzeSimulation();
    }
        private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    class UserMetric{
        int BestPossibletime=-1;
        int timeapp=-1;
        int timegetbike=-1;
        int timeretbike=-1;
        int timeleafe=-1;
        Event.RESULT_TYPE leafreason;
        int succbikereservations=0;
        int failedbikereservations=0;
        int succslotreservations=0;
        int failesslotreservations=0;
        int succbikerentals=0;
        int failedbikerentals=0;
        int succbikereturns=0;
        int failedbaikereturns=0;
    }
    class StationMetric{
        int succbikereservations=0;
        int failedbikereservations=0;
        int succslotreservations=0;
        int failesslotreservations=0;
        int succbikerentals=0;
        int failedbikerentals=0;
        int succbikereturns=0;
        int failedbaikereturns=0;
        double balancingquality=0D;
        long emtytime=0;
        int timelastchange=0;
        int currentavbikes=0;
        int capacity;
    }
    TreeMap<Integer, StationMetric> stationmetrics=new TreeMap<Integer, StationMetric>();
    TreeMap<Integer, UserMetric> usermetrics=new TreeMap<Integer, UserMetric>();
            
    private Path analysispath;
    private Path historypath;
    private int totalsimtime;
    int stations_num;
    int stations_numerwithEmptytimes=0;
    double stations_av_emptytime_stations=0;
    double stations_av_equilibrium=0;
    int users_num;
    double users_av_tostationtime=0;
    double users_av_biketime=0;
    double users_av_todesttime=0;
    double users_DS=0;
    double users_HE=0;
    double users_RE=0;
    int users_numabandon=0;
    int users_totalrentalfails=0;
    int users_totalreturnfails=0;
    
    public SimulationResultAnalyser(String analysisdir, String historydir) throws IOException {
        this.analysispath = Paths.get(analysisdir);
        this.historypath=Paths.get(historydir);
    }

    public void analyzeSimulation() throws IOException {

        // read global values
        File json = historypath.resolve("final-global-values.json").toFile();
        FileReader red = new FileReader(json);
        FinalGlobalValues fgv=gson.fromJson(red, FinalGlobalValues.class);
        totalsimtime=fgv.getTotalTimeSimulation();
        red.close();

        // setup metrics
        stationmetrics=new TreeMap<Integer, StationMetric>();
        usermetrics=new TreeMap<Integer, UserMetric>();
        
        //preprocess data (read initial station info
        preprocess();
        
        //read history
        File file = new File(historypath.toString());
        File[] directories = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        LinkedList<File> filelist=new LinkedList<File>();
        for (File histname : directories) {
            if (histname.getName().matches("\\d+\\-\\d+\\_\\d+\\.json")) {
                int i=0;
                int valuen=Integer.parseInt(histname.getName().substring(0, histname.getName().indexOf('-')));
                for (i=0; i < filelist.size(); i++){
                    int valueo=Integer.parseInt(filelist.get(i).getName().substring(0, filelist.get(i).getName().indexOf('-')));
                    if (valuen<valueo){
                       break;
                    }
                }
                filelist.add(i, histname);
            }
        } 
        for (File histname : filelist  ) {
            processHistoryEntries(readHistoryEntries(histname));
        }
        
        //write the results to files
        postprocess();
        WriteGeneraldata();
        WiteUserdata();
        WiteStationdata();
     }

    private Integer getUserID(Collection<HistoryJsonClasses.IdReference> ent){
        for (HistoryJsonClasses.IdReference ref:ent){
            if (ref.getType().equals("users")){
                return (Integer)ref.getId();
            }
        }
        throw new RuntimeException("no user id found");
    }
    private Integer getStationID(Collection<HistoryJsonClasses.IdReference> ent){
        for (HistoryJsonClasses.IdReference ref:ent){
            if (ref.getType().equals("stations")){
                return (Integer)ref.getId();
            }
        }
        return null;
    }

    private void processHistoryEntries(HistoryJsonClasses.TimeEntry[] historyentries) {
 
        //find the entries
        for (HistoryJsonClasses.TimeEntry historyentry : historyentries){
            int time=historyentry.getTime();
            if (time>totalsimtime)
                throw new RuntimeException("some error in the fistory: entry after total simulation time");
            for (HistoryJsonClasses.EventEntry ee: historyentry.getEvents()){
                Event.RESULT_TYPE result=ee.getResult();
                String name=ee.getName();
                
                Collection<HistoryJsonClasses.IdReference> involvedEntities=ee.getInvolvedEntities();
                Integer userid=getUserID(involvedEntities);
                //check new user appearance first
                if (name.equals("EventUserAppears")) {
                    UserMetric newUM=new UserMetric();
                    if (null!=usermetrics.put(userid,newUM)){
                        throw new RuntimeException("user should not exist in historyanalysis");
                    }
         //           newUM.BestPossibletime=getBesttime(userid, ee.getNewEntities()); 
                    newUM.timeapp=time;
                    continue;
                } 
                // in any other case the user should exist
                UserMetric usM=usermetrics.get(userid);
                Integer stationid=getStationID(involvedEntities);
                checkChangesAvBikes(ee.getChanges(),time,stationid);
                StationMetric stM=null;
                if (stationid!=null) stM=stationmetrics.get(stationid);
                switch (name) {
                    case "EventUserArrivesAtStationToRentBike":
                        if (result==Event.RESULT_TYPE.SUCCESSFUL_BIKE_RENTAL) {
                            stM.succbikerentals++;
                            usM.succbikerentals++;
                            usM.timegetbike=time;
                            //user rents bike
                        } else if(result==Event.RESULT_TYPE.FAILED_BIKE_RENTAL){
                            stM.failedbikerentals++;
                            usM.failedbikerentals++;
                        }   break;
                    case "EventUserArrivesAtStationToReturnBike":
                        if( result==Event.RESULT_TYPE.SUCCESSFUL_BIKE_RETURN) {
                            stM.succbikereturns++;
                            usM.succbikereturns++;
                            usM.timeretbike=time;
                            //user returns bike
                        } else if(result==Event.RESULT_TYPE.FAILED_BIKE_RETURN){
                            stM.failedbaikereturns++;
                            usM.failedbaikereturns++;
                        }   break;
                    case "EventUserTriesToReserveSlot":
                        if( result==Event.RESULT_TYPE.SUCCESSFUL_SLOT_RESERVATION) {
                            stM.succslotreservations++;
                            usM.succslotreservations++;
                        } else if( result==Event.RESULT_TYPE.FAILED_SLOT_RESERVATION) {
                            stM.failesslotreservations++;
                            usM.failesslotreservations++;
                        }   break;
                    case "EventUserTriesToReserveBike":
                        if( result==Event.RESULT_TYPE.SUCCESSFUL_BIKE_RESERVATION) {
                            stM.succbikereservations++;
                            usM.succbikereservations++;
                        } else if( result==Event.RESULT_TYPE.FAILED_BIKE_RESERVATION) {
                            stM.failedbikereservations++;
                            usM.failedbikereservations++;
                        }   break;
                    case "EventUserLeavesSystem":
                        usM.leafreason=result;
                        //user leafs
                        usM.timeleafe=time;
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    private void postprocess()  {
        //add the rest times for the stations
        stations_numerwithEmptytimes=0;
        stations_av_emptytime_stations=0;
        stations_av_equilibrium=0;
        stations_num=stationmetrics.size();
        for (StationMetric sm : stationmetrics.values()) {
            double time=totalsimtime-sm.timelastchange;
            if(sm.currentavbikes==0) {
                sm.emtytime+=time;
            }
            sm.balancingquality+=Math.abs((double)sm.currentavbikes-((double)sm.capacity/2D))*(double)time;
            sm.balancingquality=sm.balancingquality/totalsimtime;
            if (sm.emtytime>0) stations_numerwithEmptytimes++;
            stations_av_emptytime_stations+=sm.emtytime;
            stations_av_equilibrium+=sm.balancingquality;
        }
        stations_av_emptytime_stations=stations_av_emptytime_stations/(60D*(double)stations_num);
        stations_av_equilibrium=stations_av_equilibrium /(double)stations_num;
        
        //globval values for users
        users_num=usermetrics.size();
                users_av_tostationtime=0;
                users_av_biketime=0;
                users_av_todesttime=0;
                users_totalrentalfails=0;
                users_totalreturnfails=0;
        int succusers=0;
        for (UserMetric um : usermetrics.values()) {
            if (um.timegetbike!=-1) {//time counts
                succusers++;
                users_av_tostationtime+=um.timegetbike-um.timeapp;
                users_av_biketime+=um.timeretbike-um.timegetbike;
                users_av_todesttime+=um.timeleafe-um.timeretbike;
                users_totalrentalfails+=um.failedbikerentals;
                users_totalreturnfails+=um.failedbaikereturns;
            }
            if (um.leafreason!=Event.RESULT_TYPE.EXIT_AFTER_REACHING_DESTINATION) {
                users_numabandon++;
            }
        }
        if (users_num-succusers!=users_numabandon){
            throw new RuntimeException("something wrong in data analsysis");
        }
        users_av_tostationtime=users_av_tostationtime/(double)succusers;
        users_av_biketime=users_av_biketime /(double)succusers;
        users_av_todesttime=users_av_todesttime /(double)succusers;
        users_DS=(double)succusers/(double)users_num;
        users_HE=(double)succusers/(double)users_totalrentalfails+succusers;
        users_RE=(double)succusers/(double)users_totalreturnfails+succusers;
    }
    
    private void preprocess() throws IOException {
        File stfile = historypath.resolve("entities/stations.json").toFile();
        FileReader r=new FileReader(stfile);
        JsonObject jo = gson.fromJson(new FileReader(stfile), JsonObject.class);
        JsonArray st=jo.getAsJsonArray("instances");
        for (JsonElement je:st){
            HistoricStation hs=gson.fromJson(je, HistoricStation.class);
            Integer id=hs.getId();
            StationMetric sm=new StationMetric();
            sm.capacity=hs.getCapacity();
            sm.currentavbikes=hs.getAvailablebikes();
            if (stationmetrics.put(id, sm)!=null) 
                throw new RuntimeException("duplicate station");
        }
        r.close();
     }
            
    
    private void checkChangesAvBikes(Map<String, List<JsonObject>> changes, int currenttime, Integer stid){
        if (changes==null) return;
        List<JsonObject> stationch=changes.get("stations");
        if (stationch==null) return;
        for (JsonObject o:stationch){
            int id=o.get("id").getAsInt();
            if (stid.intValue()!=id)
                throw new RuntimeException("station not in involved entities but in changes");
            JsonObject o1=o.getAsJsonObject("availablebikes");
            if(o1!=null){
                int oldv=o1.get("old").getAsInt();
                int newv=o1.get("new").getAsInt();
                StationMetric sm=stationmetrics.get(id);
                if (sm.currentavbikes!=oldv)
                    throw new RuntimeException("invalid value");
                double time=currenttime-sm.timelastchange;
                if(sm.currentavbikes==0) {
                    sm.emtytime+=time;
                }
                sm.balancingquality+=Math.abs((double)sm.currentavbikes-((double)sm.capacity/2D))*(double)time;
                sm.currentavbikes=newv;
                sm.timelastchange=currenttime;
           }
        }
    }

    private static HistoryJsonClasses.TimeEntry[] readHistoryEntries(File name) throws IOException {
        // it creates a file with the specified name in the history directory
 
        // itreads the specified content from the  file
        FileReader reader = new FileReader(name);
        HistoryJsonClasses.TimeEntry[] timeentries=gson.fromJson(reader, HistoryJsonClasses.TimeEntry[].class);
        reader.close();
        return timeentries;
    }

    private void WriteGeneraldata() throws IOException {
        File outfile = this.analysispath.resolve("global_values.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        // Write empty line
        //Now set the String array for writing
        String[] record = new String[16];
        
        //write header
        record[0] = "total simulation time (min)";
        record[1] = "#users";
        record[2] = "#abandoned";
        record[3] = "DS";
        record[4] = "HE";
        record[5] = "RE";
        record[6] = "Av. time to station (min)(only succesfull users)";
        record[7] = "Av. time from orig to dest station (min)(only succesfull users)";
        record[8] = "Av. time to final destination (min)(only succesfull users)";
        record[9] = "Av. total time (min)";
        record[10] = "#failed rentals (only succesfull users)";
        record[11] = "#failed returns (only succesfull users)";
        record[12] = "#stations";
        record[13] = "#stations with empty times";
        record[14] = "Av. empty times (min)";
        record[15] = "Av. equilibrium desviation (over stations and simulationtime)";
        csvWriter.writeNext(record);

        record[0] = Integer.toString(totalsimtime);
        record[1] = Integer.toString(users_num);
        record[2] = Integer.toString(users_numabandon);
        record[3] = Double.toString(users_DS);
        record[4] = Double.toString(users_HE);
        record[5] = Double.toString(users_RE);
        record[6] = Double.toString(users_av_tostationtime);
        record[7] = Double.toString(users_av_biketime);
        record[8] = Double.toString(users_av_todesttime);
        record[9] = Double.toString(users_av_tostationtime+users_av_biketime+users_av_todesttime);
        record[10] = Integer.toString(users_totalrentalfails);
        record[11] = Integer.toString(users_totalreturnfails);
        record[12] = Integer.toString(stations_num);
        record[13] = Integer.toString(stations_numerwithEmptytimes);
        record[14] = Double.toString(stations_av_emptytime_stations);
        record[15] = Double.toString(stations_av_equilibrium);
        csvWriter.writeNext(record);
        writer.close();
    }

    private void WiteUserdata() throws IOException {
        File outfile = this.analysispath.resolve("users.csv").toFile();
        Writer writer = new FileWriter(outfile);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        //Now set the String array for writing
        String[] record = new String[13];

        //write header
        record[0] = "id";
        record[1] = "time to origin station (min)";
        record[2] = "cycling time (min)";
        record[3] = "time to destination place (min)";
        record[4] = "exit reason";
        record[5] = "Successful bike reservations";
        record[6] = "Failed bike reservations";
        record[7] = "Successful slot reservations";
        record[8] = "Failed slot reservations";
        record[9] = "Successful bike rentals";
        record[10] = "Failed bike rentals";
        record[11] = "Successful bike returns";
        record[12] = "Failed bike returns";
        csvWriter.writeNext(record);

        //now write the user values
        for (Integer id : usermetrics.keySet()) {
            UserMetric um=usermetrics.get(id);
            record[0] = Integer.toString(id);
            if (um.timegetbike==-1){
                record[1] = "";
                record[2] = "";
                record[3] = "";
            } else {
                record[1] = Double.toString((double)(um.timegetbike-um.timeapp)/60D);
                record[2] = Double.toString((double)(um.timeretbike-um.timegetbike)/60D);
                record[3] = Double.toString(((double)um.timeleafe-um.timeretbike)/60D);
            }
            record[4] = um.leafreason.toString();
            record[5] = Integer.toString(um.succbikereservations);
            record[6] = Integer.toString(um.failedbikereservations);
            record[7] = Integer.toString(um.succslotreservations);
            record[8] = Integer.toString(um.failesslotreservations);
            record[9] = Integer.toString(um.succbikerentals);
            record[10] =Integer.toString(um.failedbikerentals);
            record[11] = Integer.toString(um.succbikereturns);
            record[12] =Integer.toString(um.failedbaikereturns);
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }

    private void WiteStationdata() throws IOException {
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
            StationMetric sm=stationmetrics.get(id);
            //postprocess information
            record[0] = Integer.toString(id);
            record[1] = Double.toString((double)sm.emtytime/60D);
            record[2] = Double.toString(sm.balancingquality);
             record[3] = Integer.toString(sm.succbikereservations);
            record[4] = Integer.toString(sm.failedbikereservations);
            record[5] = Integer.toString(sm.succslotreservations);
            record[6] = Integer.toString(sm.failesslotreservations);
            record[7] = Integer.toString(sm.succbikerentals);
            record[8] =Integer.toString(sm.failedbikerentals);
            record[9] = Integer.toString(sm.succbikereturns);
            record[10] =Integer.toString(sm.failedbaikereturns);
            //write line
            csvWriter.writeNext(record);
        }
        writer.close();
    }
}

