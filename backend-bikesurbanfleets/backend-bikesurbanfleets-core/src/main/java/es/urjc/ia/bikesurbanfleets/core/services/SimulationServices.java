package es.urjc.ia.bikesurbanfleets.core.services;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.InformationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

public class SimulationServices {

    private final String INIT_EXCEPTION_MESSAGE = "Simulation Service is not correctly started."
            + " You should init all the services";

    private InfraestructureManager infrastructureManager;
    private RecommendationSystem recommendationSystem;
    private InformationSystem informationSystem;
    private GraphManager graphManager;

    private Set<Class<?>> recommendationSystemClasses;

    private Gson gson = new Gson();
 
    public SimulationServices(GlobalInfo globalInfo, List<Station> stations)
            throws IOException {

        Reflections reflections = new Reflections();
        this.recommendationSystemClasses = reflections.getTypesAnnotatedWith(RecommendationSystemType.class);

        this.infrastructureManager = new InfraestructureManager(stations, globalInfo.getBoundingBox(),globalInfo.getDemandManager());
        this.graphManager = globalInfo.getGraphManager();
        this.informationSystem = new InformationSystem(this.infrastructureManager);
        this.recommendationSystem = initRecommendationSystem(globalInfo.getRecommendationSystemTypeJsonDescription());
    }

    private RecommendationSystem initRecommendationSystem(JsonObject recsystemdef) throws IllegalStateException {

        //find the usertype
        String type = recsystemdef.get("typeName").getAsString();

        for (Class<?> recommendationSystemClass : recommendationSystemClasses) {
            String recomTypeAnnotation = recommendationSystemClass.getAnnotation(RecommendationSystemType.class).value();
            if (recomTypeAnnotation.equals(type)) {

                try {
                    Constructor constructor = recommendationSystemClass.getConstructor(JsonObject.class, InfraestructureManager.class);
                    RecommendationSystem recomSys = (RecommendationSystem) constructor.newInstance(recsystemdef, this.infrastructureManager);
                    return recomSys;
                } catch (Exception e) {
                    MessageGuiFormatter.showErrorsForGui("Error Creating Recommendation System");
                    MessageGuiFormatter.showErrorsForGui(e);
                }
            }
        }
        return null;
    }

    public InfraestructureManager getInfrastructureManager() throws IllegalStateException {
        checkService();
        return this.infrastructureManager;
    }

    public RecommendationSystem getRecommendationSystem() {
        checkService();
        return this.recommendationSystem;
    }

    public InformationSystem getInformationSystem() {
        checkService();
        return this.informationSystem;
    }

    public GraphManager getGraphManager() {
        checkService();
        return graphManager;
    }

    private void checkService() throws IllegalStateException {
        if (recommendationSystem == null || graphManager == null) {
            throw new IllegalStateException(INIT_EXCEPTION_MESSAGE);
        }
    }

}
