/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.defaultConfiguration;

/**
 *
 * @author holger
 */
public class GlobalConfigurationParameters {
    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String TEMP_DIR = HOME_DIR + "/.Bike3S";
    public static String DEBUG_DIR = TEMP_DIR;
    public static String DEFAULT_HISTORY_OUTPUT_PATH = HOME_DIR + "/history";
    public final static int TIMEENTRIES_PER_HISTORYFILE = 10000;
    
    //for checking the station ocuppation should be >0
    // used for the analysis of station data 
    public final static int STATION_OCCUPATION_CHECK_INTERVAL=300; //every 2 min
    
    // for users that wait at stations
    public final static int USERWAITING_INTERVAL=1;
    

        // the velocities here are real (estimated velocities)
        // assuming real velocities of 1.1 m/s and 4 m/s for walking and biking (aprox. to 4 and 14,4 km/h)
        //Later the velocities are adjusted to straight line velocities
        //given a straight line distance d, the real distance dr may be estimated  
        // as dr=f*d, whewre f will be between 1 and sqrt(2) (if triangle).
        // here we consider f=1.4
        //to translate velocities from realdistances to straight line distances:
        // Vel_straightline=(d/dr)*vel_real -> Vel_straightline=vel_real/f
        //assuming real velocities of 1.1 m/s and 4 m/s for walking and biking (aprox. to 4 and 14,4 km/h)
        //the adapted straight line velocities are: 0.786m/s and 2.86m/s
    //default values 
    public final static double DEFAULT_WALKING_VELOCITY=1.4D;
    public final static double DEFAULT_CYCLING_VELOCITY=4.0D;
    //The factor used to conevrt straight line distances or times or velocity to approximate real times, distance, velocitioes
    public final static double STRAIGT_LINE_FACTOR=1.7D;
    

}
