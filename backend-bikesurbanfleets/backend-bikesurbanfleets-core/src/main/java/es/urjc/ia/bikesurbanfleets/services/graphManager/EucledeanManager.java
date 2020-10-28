/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.graphManager;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;

/**
 *
 * @author holger
 */
@GraphManagerType("EUCLEDEAN")
public class EucledeanManager implements GraphManager {

    private static class GraphManParameters {
    }

    GraphManParameters parameters=null;

    public EucledeanManager(JsonObject parameterdef) throws Exception {
        this.parameters = new GraphManParameters();
        getParameters(parameterdef, this.parameters);
    }
    @Override
    public GeoRoute obtainShortestRouteBetween(GeoPoint originPoint, GeoPoint destinationPoint, String vehicle) {
            return new GeoRoute(originPoint, destinationPoint);
    }

    @Override
    public double estimateDistance(GeoPoint originPoint, GeoPoint destinationPoint, String vehicle) {
            return originPoint.eucleadeanDistanceTo(destinationPoint);
    }

    /**
     * Each graph or route manager has a velocity factor to adapt the velocities in the system 
     * to the distance calculation 
     * this affects the "expected velocity" used in the system in recommenders (defined in GlobalConfigurationParameters)
     * and also teh velocities of each user (defined in user parameters, and set un the user class)
     * When an eucleadean graph manager is user, for example, the velocity factor we use is 0,625
     * that is , a moving object moves slower in order to simulate real movements nort on a straight line
     */
    public double getVelocityFactor(){
        return GlobalConfigurationParameters.STRAIGHTLINEVELOCITYFACTOR; 
    }
    
}
