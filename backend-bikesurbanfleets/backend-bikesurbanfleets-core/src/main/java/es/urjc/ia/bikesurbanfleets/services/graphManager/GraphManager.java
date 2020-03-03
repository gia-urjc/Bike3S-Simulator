package es.urjc.ia.bikesurbanfleets.services.graphManager;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import static es.urjc.ia.bikesurbanfleets.common.util.ReflectiveClassFinder.findClass;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import java.lang.reflect.Constructor;

import java.util.List;
import org.reflections.Reflections;

/**
 * This interface provides methods to manage the routes of a geographic map.
 * @author IAgroup
 *
 */
public interface GraphManager {
    
    static GraphManager getGraphManager(GlobalInfo globalInfo) throws Exception{
        //setup the graph manager
        System.out.println("load GraphManager");
        GraphManager gm;
        Class c =findClass(globalInfo.getgraphManagerJsonDescription(), GraphManagerType.class);
        if (c == null) {
            gm= null;
        }
        Constructor constructor = c.getConstructor(JsonObject.class);
        gm= (GraphManager) constructor.newInstance(globalInfo.getgraphManagerJsonDescription());
        if (gm != null) {
            System.out.println("graphManager loaded");
        } else {
            System.out.println("!!no graphManager loaded");
        }
        return gm;
     }
 
    /**
     * It calculates which is the shortest route.
     * @return the shortest route of all posible routes between 2 points.
     * @throws GeoRouteCreationException
     * @throws GraphHopperIntegrationException
     */
    GeoRoute obtainShortestRouteBetween(GeoPoint originPoint, GeoPoint destinationPoint, String vehicle);

    /**
     * It estimates the distance between two points either by "bike" or by "foot"
     * @return the shortest route of all posible routes between 2 points.
     * @throws GeoRouteCreationException
     * @throws GraphHopperIntegrationException
     */
    double estimateDistance(GeoPoint originPoint, GeoPoint destinationPoint, String vehicle);
}
