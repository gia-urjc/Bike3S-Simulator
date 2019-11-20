package es.urjc.ia.bikesurbanfleets.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    public SimulationServices() {
    }

    public void initSimulationServices(GlobalInfo globalInfo, List<Station> stations) throws Exception {
        Reflections reflections = new Reflections("es.urjc.ia.bikesurbanfleets");
        //setup the infrastructureManager
        this.stationManager = new StationManager(stations);
        //setup the information system
        this.informationSystem = new InformationSystem(this.stationManager);

        //setup the demandManager
        if (globalInfo.getdemandManagerJsonDescription() != null && globalInfo.getdemandManagerJsonDescription().has("typeName")) {
            System.out.println("load demandManager");
            this.demandManager = (DemandManager) getDemandManager(
                    findManagerClass(reflections, globalInfo.getdemandManagerJsonDescription(), DemandManagerType.class, "DemandManager"),
                    globalInfo.getdemandManagerJsonDescription());
            if (demandManager != null) {
                System.out.println("demandManager loaded");
            } else {
                System.out.println("!!no demandManager loaded");
            }
        } else {
            System.out.println("!!no demandManager loaded");
        }

        //setup the graph manager
        System.out.println("load GraphManager");
        this.graphManager = (GraphManager) getGraphManager(
                findManagerClass(reflections, globalInfo.getgraphManagerJsonDescription(), GraphManagerType.class, "GraphManager"),
                globalInfo.getgraphManagerJsonDescription());
        if (graphManager != null) {
            System.out.println("graphManager loaded");
        } else {
            System.out.println("!!no graphManager loaded");
        }

        //setup the recomendation system
        if (globalInfo.getRecommendationSystemJsonDescription() != null && globalInfo.getRecommendationSystemJsonDescription().has("typeName")) {
            System.out.println("load recommendationSystem");
            this.recommendationSystem = (RecommendationSystem) getRecommendationSystem(
                    findManagerClass(reflections, globalInfo.getRecommendationSystemJsonDescription(), RecommendationSystemType.class, "RecommendationSystem"),
                    globalInfo.getRecommendationSystemJsonDescription());
            if (recommendationSystem != null) {
                System.out.println("recommendationSystem loaded");
            } else {
                System.out.println("!!no recommendationSystem loaded");
            }
        } else {
            System.out.println("!!no recommendationSystem loaded");
        }

        //setup the fleetManagersystem
        if (globalInfo.getFleetManagerJsonDescription() != null && globalInfo.getFleetManagerJsonDescription().has("typeName")) {
            System.out.println("load fleetManager");
            this.fleetManager = (FleetManager) getFleetManager(
                    findManagerClass(reflections, globalInfo.getFleetManagerJsonDescription(), FleetManagerType.class, "FleetManager"),
                    globalInfo.getFleetManagerJsonDescription());
            if (fleetManager != null) {
                System.out.println("fleetManager loaded");
            } else {
                System.out.println("!!no fleetManager loaded");
            }
        } else {
            this.fleetManager = (FleetManager) getFleetManager(DummyFleetManager.class,null);
            System.out.println("DummyFleetManager loaded by default");
        }
        checkService();
    }

    private Class findManagerClass(Reflections reflections, JsonObject jsondescription, Class annotationclasstype, String servicetype) throws Exception {
        if (jsondescription == null) {
            return null;
        }
        String type = jsondescription.get("typeName").getAsString();
        if (type.equals("none")) {
            return null;
        }

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotationclasstype);
        for (Class<?> this_class : classes) {
            Annotation a = this_class.getAnnotation(annotationclasstype);
            Method m = a.getClass().getMethod("value");
            String typeAnnotation = (String) m.invoke(a);
            if (typeAnnotation.equals(type)) {
                return this_class;
            }
        }
        throw new RuntimeException(servicetype + " " + type + " not found or incorrect");
    }

    private GraphManager getGraphManager(Class c, JsonObject jsondescription) throws Exception {
        if (c == null) {
            return null;
        }
        Constructor constructor = c.getConstructor(JsonObject.class);
        return (GraphManager) constructor.newInstance(jsondescription);
    }

    private RecommendationSystem getRecommendationSystem(Class c, JsonObject jsondescription) throws Exception {
        if (c == null) {
            return null;
        }
        Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
        return (RecommendationSystem) constructor.newInstance(jsondescription, this);
    }

    private DemandManager getDemandManager(Class c, JsonObject jsondescription) throws Exception {
        if (c == null) {
            return null;
        }
        Constructor constructor = c.getConstructor(JsonObject.class);
        return (DemandManager) constructor.newInstance(jsondescription);
    }

    private FleetManager getFleetManager(Class c, JsonObject jsondescription) throws Exception {
        if (c == null) {
            return null;
        }
        Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
        return (FleetManager) constructor.newInstance(jsondescription, this);
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
