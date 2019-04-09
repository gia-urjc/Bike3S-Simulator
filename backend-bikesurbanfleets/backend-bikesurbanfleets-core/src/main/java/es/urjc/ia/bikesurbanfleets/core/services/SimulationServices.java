package es.urjc.ia.bikesurbanfleets.core.services;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManagerParameters;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManagerType;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.InformationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SimulationServices {

    private final String INIT_EXCEPTION_MESSAGE = "Simulation Service is not correctly started."
            + " You should init all the services";

    private InfrastructureManager infrastructureManager;
    private RecommendationSystem recommendationSystem;
    private InformationSystem informationSystem;
    private GraphManager graphManager;
    private DemandManager demandManager;
    
    private Gson gson = new Gson();
 
    public SimulationServices(){    }
    
    public void initSimulationServices(GlobalInfo globalInfo, List<Station> stations) throws IOException{
        //setup the demandManager
        this.demandManager = initDemandManager(globalInfo.isLoadDemandData(), globalInfo.getDemandDataFilePath());
        //setup the infrastructureManager
        this.infrastructureManager = new InfrastructureManager(stations);
        //setup the information system
        this.informationSystem = new InformationSystem(this.infrastructureManager);
        
        Reflections reflections = new Reflections();
        //setup the graph manager
        this.graphManager = initGraphManager(reflections, globalInfo.getGraphManagerType(), 
                globalInfo.getGraphParameters(), GlobalInfo.TEMP_DIR);
        //setup the recomendation system
        this.recommendationSystem = initRecommendationSystem(reflections, globalInfo.getRecommendationSystemTypeJsonDescription());
        checkService();
    }
    
    private GraphManager initGraphManager(Reflections reflections, String graphManagerType, 
            JsonObject graphParameters, String tempdir) {
        System.out.println("load GraphManager");
        Gson gson = new Gson();

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
                        return  (GraphManager) constructor.newInstance(gson.fromJson(graphParameters, graphParametersClass), tempdir);
                    } else {
                        Constructor constructor = graphClass.getConstructor();
                        return (GraphManager) constructor.newInstance(tempdir);
                    }
                } catch (Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating Graph Manager");
                    throw new RuntimeException("Error Creating Graph Manager");
                }
            }
        }
        System.out.println("GraphManager loaded");
        return null;
    }

    private DemandManager initDemandManager( boolean load, String file) {
        DemandManager d=null;
        if (load){
            d = new DemandManager();
            d.ReadData(file);
        } 
        return d;
     }

    private RecommendationSystem initRecommendationSystem(Reflections reflections, JsonObject recsystemdef) throws IllegalStateException {

        //find the recomendersystemtype
        Set<Class<?>> recommendationSystemClasses = reflections.getTypesAnnotatedWith(RecommendationSystemType.class);

        String type = recsystemdef.get("typeName").getAsString();

        for (Class<?> recommendationSystemClass : recommendationSystemClasses) {
            String recomTypeAnnotation = recommendationSystemClass.getAnnotation(RecommendationSystemType.class).value();
            if (recomTypeAnnotation.equals(type)) {

                try {
                    Constructor constructor = recommendationSystemClass.getConstructor(JsonObject.class, SimulationServices.class);
                    RecommendationSystem recomSys = (RecommendationSystem) constructor.newInstance(recsystemdef, this);
                    return recomSys;
                } catch (Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating Recommendation System");
                    MessageGuiFormatter.showErrorsForGui(e);
                }
            }
        }
        return null;
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
        if (recommendationSystem == null || graphManager == null || informationSystem==null || infrastructureManager==null) {
            throw new IllegalStateException(INIT_EXCEPTION_MESSAGE);
        }
    }

}
