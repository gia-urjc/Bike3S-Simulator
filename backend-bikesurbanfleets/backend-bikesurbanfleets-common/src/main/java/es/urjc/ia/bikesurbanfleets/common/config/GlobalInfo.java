package es.urjc.ia.bikesurbanfleets.common.config;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

public class GlobalInfo {

    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String TEMP_DIR = HOME_DIR + "/.Bike3S";
    public static String DEBUG_DIR = TEMP_DIR;
    private static String DEFAULT_HISTORY_OUTPUT_PATH = HOME_DIR+"/history";
    public final static int TIMEENTRIES_PER_HISTORYFILE = 1000;

    /**
     * It is the time period during a reservation is valid or active.
     */
    private int reservationTime;

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
    private long randomSeed;

    /**
     * It delimits the simulation area.
     */
    private BoundingBox boundingBox;

    /**
     * Path where history files stored
     */
    private String historyOutputPath=DEFAULT_HISTORY_OUTPUT_PATH;
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

    public int getReservationTime() {
        return reservationTime;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public void setRecommendationSystemType(JsonObject recommendationSystemType) {
        this.recommendationSystemType = recommendationSystemType;
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

    public void setHistoryOutputPath(String historyOutputPath) {
        this.historyOutputPath = historyOutputPath;
    }

    public JsonObject getRecommendationSystemTypeJsonDescription() {
        return recommendationSystemType;
    }

    public String getGraphManagerType() {
        return graphManagerType;
    }

    public void setGraphParameters(String mapPath) {
        // TODO make it flexible to different properties
        this.graphParameters = new JsonObject();
        graphParameters.addProperty("mapDir", mapPath);
    }

    public JsonObject getGraphParameters() {
        return graphParameters;
    }

}
