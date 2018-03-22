package es.urjc.ia.bikesurbanfleets.common.config;

import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

public class GlobalInfo {

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
    private boolean debugMode;

    /**
     * It is the seed which initializes the random instance.
     */
    private long randomSeed;

    /**
     * It is the absolute route of the used map.
     */
    private String map;

    /**
     * It delimits the simulation area.
     */
    private BoundingBox boundingBox;

    /**
     * Path  where history files stored
     */
    private String historyOutputPath;

    /**
     *
     */
    private boolean linearDistance;


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

    public String getMap() {
        return map;
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

    public boolean getLinearDistance() {
        return linearDistance;
    }

}
