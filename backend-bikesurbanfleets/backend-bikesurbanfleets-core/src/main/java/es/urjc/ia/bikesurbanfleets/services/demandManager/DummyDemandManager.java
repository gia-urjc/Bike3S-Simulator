/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.demandManager;

import com.google.gson.JsonObject;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 *
 * @author holger
 */
   
@DemandManagerType("DummyDemandManager")
public class DummyDemandManager extends DemandManager{

    private static class DemandManParameters {
    }

    DemandManParameters parameters=null;
    
    public DummyDemandManager(JsonObject parameterdef) throws Exception {
        super();
        this.parameters = new DemandManParameters();
        getParameters(parameterdef, this.parameters);
    }

    public  double getStationTakeRateIntervall(int stationID, LocalDateTime start, double endtimeoffset) {return 0;} ;
    public  double getStationReturnRateIntervall(int stationID, LocalDateTime start, double endtimeoffset) {return 0;} ;
    public  double getGlobalTakeRateIntervall(LocalDateTime start, double endtimeoffset) {return 0;} ;
    public  double getGlobalReturnRateIntervall(LocalDateTime start, double endtimeoffset){return 0;}  ;

    public  double getStationTakeRatePerHour(int stationID, LocalDateTime t) {return 0;} ;
    public  double getStationReturnRatePerHour(int stationID, LocalDateTime t) {return 0;} ;

    public  double getGlobalTakeRatePerHour(LocalDateTime t) {return 0;} ;
    public  double getGlobalReturnRatePerHour(LocalDateTime t) {return 0;} ;
   
    public  double getStationTakeRatePerHour(int stationID, Month month, WeekDay day, int hour) {return 0;} ;
    public  double getStationReturnRatePerHour(int stationID, Month month, WeekDay day, int hour) {return 0;} ;

    public  double getGlobalTakeRatePerHour(Month month, WeekDay day, int hour){return 0;}  ;
    public  double getGlobalReturnRatePerHour(Month month, WeekDay day, int hour) {return 0;} ;

}
