/*
 * This class represents the current time and date of the sim ulation
 */
package es.urjc.ia.bikesurbanfleets.core.core;

import java.time.LocalDateTime;

/**
 *
 * @author holger
 */
public class SimulationDateTime {
    static boolean dateTimeSet=false;
    static private LocalDateTime initialSimulationDateTime;
    //represents the current date and time of the simulation
    static private LocalDateTime currentSimulationDateTime;
    //represents tha current simulation instant in seconds from the start of the simulationsecond
    static private long currentSimulationInstant;
    
    // startdatetime should be a string in the form: 2007-12-03T10:15:30
    static public void intSimulationDateTime(String startdatetime){
        if (startdatetime==null){
            System.out.println("Date and Time of the simulation: not set");
            dateTimeSet=false;
            currentSimulationDateTime=null;
            initialSimulationDateTime=null;
        } else{
            dateTimeSet=true;
            System.out.println("Date and Time of the simulation: "+startdatetime);
            currentSimulationDateTime=LocalDateTime.parse(startdatetime);
            initialSimulationDateTime=LocalDateTime.parse(startdatetime);
        }  
        currentSimulationInstant=0;
    }

    public static LocalDateTime getCurrentSimulationDateTime() {
        if (dateTimeSet) return currentSimulationDateTime;
        return null;
    }

    public static long getCurrentSimulationInstant() {
        return currentSimulationInstant;
    }

    public static void setCurrentSimulationInstant(long current) {
        currentSimulationInstant = current;
        if (dateTimeSet) {
            currentSimulationDateTime=initialSimulationDateTime.plusSeconds(current);
        }
    }
}
