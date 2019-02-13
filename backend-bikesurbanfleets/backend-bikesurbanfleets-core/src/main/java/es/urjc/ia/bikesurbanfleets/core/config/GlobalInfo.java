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
     * if true, demand data will be loaded.
     * if false or not present in the json description, demand data will not be used
     */
    private boolean loadDemandData=false;

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
    /**
     * Path where deman data file is written
     */
    private String demandDataFilePath;
    /**
     * Recommendation system that will be used by the agents
     */
    private JsonObject recommendationSystemType;

    /**
     * Graph implementation that will be used by the agents to get routes
     * between points
     */
    private String graphManagerType;

    private JsonObject graphParameters;

    public String getGraphManagerType() {
        return graphManagerType;
    }

    public JsonObject getGraphParameters() {
        return graphParameters;
    }

    public int getReservationTime() {
        return reservationTime;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public void setOtherRecommendationSystemType(JsonObject recommendationSystemType) {
        this.recommendationSystemType = recommendationSystemType;
    }

    public JsonObject getRecommendationSystemTypeJsonDescription() {
        return this.recommendationSystemType;
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

    public void setOtherDemandDataFilePath(String demandDataFilePath) {
        this.demandDataFilePath = demandDataFilePath;
    }

    public boolean isLoadDemandData() {
        return loadDemandData;
    }

    public String getDemandDataFilePath() {
        return demandDataFilePath;
    }

    public void setOtherGraphParameters(String mapPath) {
        // TODO make it flexible to different properties
        this.graphParameters = new JsonObject();
        graphParameters.addProperty("mapDir", mapPath);
    }

    public String getStartDateTime() {
        return startDateTime;
    }
    
}
