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
    public final static int STATION_OCCUPATION_CHECK_INTERVAL = 300; //every 2 min

    // for users that wait at stations
    public final static int USERWAITING_INTERVAL = 1;

    // the velocities here are real (estimated velocities)
    // assuming real velocities of 1.4 m/s and 4 m/s for walking and biking (aprox. to 4 and 14,4 km/h)
    //standard values used in other simulations
    //default values 
    //these values are used in the recommednation services
    // theuy are also used as defeult velocities for users, if no other velocities are specified for a user
    public final static double DEFAULT_WALKING_VELOCITY = 1.4D;
    public final static double DEFAULT_CYCLING_VELOCITY = 4.0D;

    // for moving time estimations based on straightline distances we use
    // a velocity factor of 0.614.
    // that is an object moving with velocity v but simulated on a straight line (instead on a real graph) 
    // will move in the simulation with a velocity of v*STRAIGHTLINEVELOCITYFACTOR
    //that means it will move slower in order to simulate de real distances
    public final static double STRAIGHTLINEVELOCITYFACTOR = 0.614D;

}
