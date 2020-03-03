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

    
}
