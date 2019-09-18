package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

public class GlobalInfo {

    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String TEMP_DIR = HOME_DIR + "/.Bike3S";
    public static String DEBUG_DIR = TEMP_DIR;
    private static String DEFAULT_HISTORY_OUTPUT_PATH = HOME_DIR + "/history";
    public final static int TIMEENTRIES_PER_HISTORYFILE = 10000;

    /**
     * It is the time period during a reservation is valid or active.
     */
    private int reservationTime;

    /**
     * It is the start date and time of a simulation.
     * if null, (and no date is specified in the jason file) 
     * no date and time data is used
     */
    private String startDateTime=null;
    /**
     * It is the moment when the system stops creating entry points for the
     * simulation, so it ends.
     */
    private int totalSimulationTime;

    /**
     * If true, user log files will be generated for debug purposes
     */
    private boolean debugMode = false;

    /**
     * It is the seed which initializes the random instance.
     */
    private long randomSeed=23;

    /**
     * It delimits the simulation area.
     */
    private BoundingBox boundingBox;

    /**
     * Path where history files stored
     */
    private String historyOutputPath = DEFAULT_HISTORY_OUTPUT_PATH;

     public int getReservationTime() {
        return reservationTime;
    }
    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String getHistoryOutputPath() {
        return historyOutputPath;
    }

    public void setOtherHistoryOutputPath(String historyOutputPath) {
        this.historyOutputPath = historyOutputPath;
    }
    
    public String getStartDateTime() {
        return startDateTime;
    }
    
    
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // configuration of the different services that maight be flexible (implemented by different classes)
    /**
     * Recommendation system that will be used by the agents
     */
    private JsonObject recommendationSystem=null;
    public void setOtherRecommendationSystem(JsonObject recommendationSystem) {
        this.recommendationSystem = recommendationSystem;
    }
    public JsonObject getRecommendationSystemJsonDescription() {
        return this.recommendationSystem;
    }

    //demandmanager
    private JsonObject demandManager=null;
    public JsonObject getdemandManagerJsonDescription() {
        return this.demandManager;
    }

    /**
     * Fleet manager that will be used 
     */
    private JsonObject fleetManager=null;
    public JsonObject getFleetManagerJsonDescription() {
        return this.fleetManager;
    }
   
    /**
     * Graph implementation that will be used by the agents to get routes
     * between points
     */
    private JsonObject graphManager=null;
    public JsonObject getgraphManagerJsonDescription() {
        return this.graphManager;
    }

}
