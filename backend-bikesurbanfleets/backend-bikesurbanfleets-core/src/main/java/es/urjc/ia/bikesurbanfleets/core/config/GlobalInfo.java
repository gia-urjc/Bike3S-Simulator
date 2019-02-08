package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManagerParameters;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManagerType;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

public class GlobalInfo {

    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String TEMP_DIR = HOME_DIR + "/.Bike3S";
    public static String DEBUG_DIR = TEMP_DIR;
    private static String DEFAULT_HISTORY_OUTPUT_PATH = HOME_DIR + "/history";
    public final static int TIMEENTRIES_PER_HISTORYFILE = 10000;

    /**
     * if true, demand data will be loaded.
     */
    private boolean loadDemandData;

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

    //the GraphManager for the simulation
    private GraphManager graphManager;
    //the DemandManager for teh simulation
    private DemandManager demandManager;

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

    public void setOtherGraphParameters(String mapPath) {
        // TODO make it flexible to different properties
        this.graphParameters = new JsonObject();
        graphParameters.addProperty("mapDir", mapPath);
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public DemandManager getDemandManager() {
        return demandManager;
    }

    //ints the graph manager and the demand manager
    public void initGlobalManagerObjects() {
        System.out.println("load GraphManager");
        initGraphManager();
        System.out.println("GraphManager loaded");
        if (loadDemandData) {
            System.out.println("load DemandManager");
            initDemandManager();
            System.out.println("DemandManager loaded");
        }
    }

    private void initGraphManager() {
        Gson gson = new Gson();

        Reflections reflections = new Reflections();
        Set<Class<?>> graphClasses = reflections.getTypesAnnotatedWith(GraphManagerType.class);

        for (Class<?> graphClass : graphClasses) {
            String graphTypeAnnotation = graphClass.getAnnotation(GraphManagerType.class).value();
            if (graphTypeAnnotation.equals(graphManagerType)) {
                List<Class<?>> innerClasses = Arrays.asList(graphClass.getClasses());
                Class<?> graphParametersClass = null;

                for (Class<?> innerClass : innerClasses) {
                    if (innerClass.getAnnotation(GraphManagerParameters.class) != null) {
                        graphParametersClass = innerClass;
                        break;
                    }
                }

                try {
                    if (graphParametersClass != null) {
                        Constructor constructor = graphClass.getConstructor(graphParametersClass, String.class);
                        graphManager = (GraphManager) constructor.newInstance(gson.fromJson(graphParameters, graphParametersClass), TEMP_DIR);
                    } else {
                        Constructor constructor = graphClass.getConstructor();
                        graphManager = (GraphManager) constructor.newInstance(TEMP_DIR);
                    }
                } catch (Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating Graph Manager");
                    throw new RuntimeException("Error Creating Graph Manager");
                }
            }
        }
        if (graphManager == null) {
            MessageGuiFormatter.showErrorsForGui("Error Creating Graph Manager");
            throw new RuntimeException("Error Creating Graph Manager");
        }

    }

    private void initDemandManager() {
        demandManager = new DemandManager();
        demandManager.ReadData(demandDataFilePath);
    }

}
