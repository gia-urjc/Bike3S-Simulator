package es.urjc.ia.bikesurbanfleets.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static es.urjc.ia.bikesurbanfleets.common.util.ReflectiveClassFinder.findClass;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManagerType;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.DummyFleetManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManagerType;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManagerType;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

public class SimulationServices {

    private final String INIT_EXCEPTION_MESSAGE = "Simulation Service is not correctly started."
            + " You should init all the services";

    private StationManager stationManager;
    private RecommendationSystem recommendationSystem;
    private InformationSystem informationSystem;
    private GraphManager graphManager;
    private DemandManager demandManager;
    private FleetManager fleetManager;

    private Gson gson = new Gson();

    public SimulationServices(GraphManager gm) {
        this.graphManager = gm;
    }

    public void initSimulationServices(GlobalInfo globalInfo, List<Station> stations) throws Exception {
        //setup the infrastructureManager
        this.stationManager = new StationManager(stations);

        //setup the demandManager
        getDemandManager(globalInfo.getdemandManagerJsonDescription());

        //setup the information system
        this.informationSystem = new InformationSystem(this.stationManager, this.graphManager);

        //setup the recomendation system
        getRecommendationSystem(globalInfo.getRecommendationSystemJsonDescription());

        //setup the fleetManagersystem
        getFleetManager(globalInfo.getFleetManagerJsonDescription());
        
        checkService();
    }

    private void getRecommendationSystem(JsonObject jsondescription) throws Exception {
        this.recommendationSystem = null;
        if (jsondescription != null && jsondescription.has("typeName")) {
            System.out.println("load recommendationSystem");
            Class c = findClass(jsondescription, RecommendationSystemType.class);
            if (c != null) {
                Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
                this.recommendationSystem = (RecommendationSystem) constructor.newInstance(jsondescription, this);
            }
        }
        if (recommendationSystem != null) {
            System.out.println("recommendationSystem loaded");
        } else {
            System.out.println("!!no recommendationSystem loaded");
        }
    }

    private void getDemandManager(JsonObject jsondescription) throws Exception {
        this.demandManager = null;
        if (jsondescription != null && jsondescription.has("typeName")) {
            System.out.println("load demandManager");
            Class c = findClass(jsondescription, DemandManagerType.class);
            if (c != null) {
                Constructor constructor = c.getConstructor(JsonObject.class);
                this.demandManager = (DemandManager) constructor.newInstance(jsondescription);
            }
        }
        if (demandManager != null) {
            System.out.println("demandManager loaded");
        } else {
            System.out.println("!!no demandManager loaded");
        }
    }

    private void getFleetManager(JsonObject jsondescription) throws Exception {
        this.fleetManager = null;
        if (jsondescription != null && jsondescription.has("typeName")) {
            System.out.println("load fleetManager");
            Class c = findClass(jsondescription, FleetManagerType.class);
            if (c != null) {
                Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
                this.fleetManager = (FleetManager) constructor.newInstance(jsondescription, this);
            }
        }
        if (fleetManager != null) {
            System.out.println("fleetManager loaded");
        } else {
            this.fleetManager = new DummyFleetManager(null, this);
            System.out.println("DummyFleetManager loaded by default");
        }
    }

    public FleetManager getFleetManager() throws IllegalStateException {
        return this.fleetManager;
    }

    public StationManager getStationManager() throws IllegalStateException {
        return this.stationManager;
    }

    public RecommendationSystem getRecommendationSystem() {
        return this.recommendationSystem;
    }

    public InformationSystem getInformationSystem() {
        return this.informationSystem;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public DemandManager getDemandManager() {
        return demandManager;
    }

    private void checkService() throws IllegalStateException {
        if (graphManager == null || informationSystem == null || stationManager == null) {
            throw new IllegalStateException(INIT_EXCEPTION_MESSAGE);
        }
    }

}
