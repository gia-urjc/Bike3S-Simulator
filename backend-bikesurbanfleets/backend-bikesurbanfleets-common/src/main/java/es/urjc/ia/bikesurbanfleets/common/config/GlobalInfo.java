package es.urjc.ia.bikesurbanfleets.common.config;

import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

public class GlobalInfo {

    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String AUX_DIR = HOME_DIR + "/.Bike3S";

    /**
     * It is the time period during a reservation is valid or active.
     */
    private int reservationTime;

    /**
     * It is the moment when the system stops creating entry points for the simulation, so it ends.
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
     * Path  where history files stored
     */
    private String historyOutputPath;
    /**
     * Recommendation system that will be used by the agents
     */
    private String recommendationSystemType;

    /**
     * Graph implementation that will be used by the agents to get routes between points
     */
    private String graphManagerType;

    /**
     * Max distance for recommendation system
     */
    private int maxDistanceRecommendation;

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

    public void setHistoryOutputPath(String historyOutputPath) {
        this.historyOutputPath = historyOutputPath;
    }

    public String getRecommendationSystemType() {
        return recommendationSystemType;
    }

    public String getGraphManagerType() {
        return graphManagerType;
    }

    public int getMaxDistanceRecommendation() {
        return maxDistanceRecommendation;
    }

}
