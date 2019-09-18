package es.urjc.ia.bikesurbanfleets.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManagerType;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManager;
import es.urjc.ia.bikesurbanfleets.services.fleetManager.FleetManagerType;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManagerType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

public class SimulationServices {

    private final String INIT_EXCEPTION_MESSAGE = "Simulation Service is not correctly started."
            + " You should init all the services";

    private InfrastructureManager infrastructureManager;
    private RecommendationSystem recommendationSystem;
    private InformationSystem informationSystem;
    private GraphManager graphManager;
    private DemandManager demandManager;
    private FleetManager fleetManager;
    
    private Gson gson = new Gson();
 
    public SimulationServices(){    }
    
    public void initSimulationServices(GlobalInfo globalInfo, List<Station> stations) throws Exception{
       Reflections reflections = new Reflections();
        //setup the infrastructureManager
        this.infrastructureManager = new InfrastructureManager(stations);
        //setup the information system
        this.informationSystem = new InformationSystem(this.infrastructureManager);
        
        //setup the demandManager
        
        this.demandManager = (DemandManager) getDemandManager(
                findManagerClass(reflections, globalInfo.getdemandManagerJsonDescription(), DemandManagerType.class, "DemandManager"),
                globalInfo.getdemandManagerJsonDescription());
        //setup the graph manager
        this.graphManager = (GraphManager) getGraphManager(
                findManagerClass(reflections, globalInfo.getgraphManagerJsonDescription(), GraphManagerType.class, "GraphManager"),
                globalInfo.getgraphManagerJsonDescription());
        //setup the recomendation system
        this.recommendationSystem = (RecommendationSystem) getRecommendationSystem(
                findManagerClass(reflections, globalInfo.getRecommendationSystemJsonDescription(), RecommendationSystemType.class, "Recommendation System"),
                globalInfo.getRecommendationSystemJsonDescription());
        //setup the fleetManagersystem
        this.fleetManager = (FleetManager) getFleetManager(
                findManagerClass(reflections, globalInfo.getFleetManagerJsonDescription(), FleetManagerType.class, "FleetManager"),
                globalInfo.getFleetManagerJsonDescription());
        checkService();
    }
    
    private Class findManagerClass(Reflections reflections, JsonObject jsondescription, Class annotationclasstype, String servicetype) throws Exception{
        System.out.println("load " + servicetype);
        if (jsondescription==null) {
            System.out.println("no " + servicetype + " loaded");
            return null;
        }
        String type = jsondescription.get("typeName").getAsString();
        if (type.equals("none")) {
            System.out.println("no " + servicetype + " loaded");
            return null;
        }

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotationclasstype);
        for (Class<?> this_class : classes) {
            Annotation a=this_class.getAnnotation(annotationclasstype);
            Method m=a.getClass().getMethod("value");
            String typeAnnotation = (String)m.invoke(a);
            if (typeAnnotation.equals(type)) {
                return this_class;
            }
        }
        throw new RuntimeException( servicetype + " " + type + " not found or incorrect");
    }
    
    private GraphManager getGraphManager(Class c, JsonObject jsondescription) throws Exception {
        if (c==null) return null;
        Constructor constructor = c.getConstructor(JsonObject.class);
        return  (GraphManager) constructor.newInstance(jsondescription);
    }
    private RecommendationSystem getRecommendationSystem(Class c, JsonObject jsondescription) throws Exception {
        if (c==null) return null;
        Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
        return  (RecommendationSystem) constructor.newInstance(jsondescription, this);
    }
    private DemandManager getDemandManager(Class c, JsonObject jsondescription) throws Exception {
        if (c==null) return null;
        Constructor constructor = c.getConstructor(JsonObject.class);
        return  (DemandManager) constructor.newInstance(jsondescription);
    }
    private FleetManager getFleetManager(Class c, JsonObject jsondescription) throws Exception {
        if (c==null) return null;
        Constructor constructor = c.getConstructor(JsonObject.class, SimulationServices.class);
        return  (FleetManager) constructor.newInstance(jsondescription, this);
    }

    public FleetManager getFleetManager() throws IllegalStateException {
        return this.fleetManager;
    }
    public InfrastructureManager getInfrastructureManager() throws IllegalStateException {
        return this.infrastructureManager;
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
        if ( graphManager == null || informationSystem==null || infrastructureManager==null) {
            throw new IllegalStateException(INIT_EXCEPTION_MESSAGE);
        }
    }

}
