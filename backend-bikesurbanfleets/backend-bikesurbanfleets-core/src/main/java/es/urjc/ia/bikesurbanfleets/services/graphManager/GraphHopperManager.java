package es.urjc.ia.bikesurbanfleets.services.graphManager;

import com.google.gson.JsonObject;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.util.CheckSum;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.RecommendationSystemByAvailableResources;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@GraphManagerType("GRAPH_HOPPER")
public class GraphHopperManager implements GraphManager {

    private static class GraphManParameters {
        private String mapFile=null;
        private String tempDirectory=null;
    }

    GraphManParameters parameters=null;
    private GraphHopper hopper;
    private GeoRoute lastroute;

    private GeoPoint laststartPosition;
    private GeoPoint lastendPosition;
    private String lastVehicle="";

    public GraphHopperManager(JsonObject parameterdef) throws Exception {
        this.parameters = new GraphManParameters();
        getParameters(parameterdef, this.parameters);
        setup(parameters.tempDirectory);
    }
    public GraphHopperManager(String mapFile, String temp_dir) throws IOException {
        this.parameters = new GraphManParameters();
        parameters.mapFile=mapFile;
        parameters.tempDirectory=temp_dir;
        setup(parameters.tempDirectory);
    }
    private void setup(String temp_dir) throws IOException {
    
        String GRAPHHOPPER_DIR = temp_dir + "/graphhopper_files";
       //Check the last map loaded
        boolean sameMap;
        try {
            CheckSum csum = new CheckSum(temp_dir);
            sameMap = csum.md5CheckSum(temp_dir, new File(parameters.mapFile));
        }
        catch (Exception e) {
            sameMap = false;
        }

        //If it is not the same map, we remove the latest temporary files
        if(!sameMap) {
            FileUtils.deleteDirectory(new File(GRAPHHOPPER_DIR));
        }
        this.hopper = new GraphHopperOSM().forServer();
        hopper.setDataReaderFile(parameters.mapFile);
        hopper.setGraphHopperLocation(GRAPHHOPPER_DIR);
        hopper.setEncodingManager(new EncodingManager("foot, bike"));
        hopper.importOrLoad();
    }

    private static GeoRoute responseGHToRoute(PathWrapper path, GeoPoint start, GeoPoint end) throws GeoRouteCreationException{
        List<GeoPoint> geoPointList = new ArrayList<>();
        PointList ghPointList = path.getPoints();
        geoPointList.add(start);
        GHPoint3D prev = null;
        for(GHPoint3D p: ghPointList) {
            if(prev != null && p.equals(prev)){
                continue;
            }
            geoPointList.add(new GeoPoint(p.getLat(), p.getLon()));
            prev = p;
        }
        geoPointList.add(end);
        GeoRoute route = new GeoRoute(geoPointList);
        return route;
    }

    private GeoRoute gotItAlready(GeoPoint startPosition, GeoPoint endPosition, String vehicle){
        if(startPosition.equals(this.laststartPosition) 
                && endPosition.equals(this.lastendPosition)
                && vehicle.equals(lastVehicle)) return this.lastroute;
        else return null;        
    }

    private void calculateRoute(GeoPoint startPosition, GeoPoint endPosition, String vehicle) throws GraphHopperIntegrationException, GeoRouteCreationException  {
        if(gotItAlready(startPosition, endPosition, vehicle)==null) {
             GHRequest req = new GHRequest(
                    startPosition.getLatitude(), startPosition.getLongitude(),
                    endPosition.getLatitude(), endPosition.getLongitude())
                    .setWeighting("fastest")
                    .setVehicle(vehicle);
            GHResponse rsp = hopper.route(req);

            if(rsp.hasErrors()) {
                for(Throwable exception: rsp.getErrors()) {
                    throw new GraphHopperIntegrationException(exception.getMessage());
                }
            }
            PathWrapper path = rsp.getBest();
            this.lastroute = responseGHToRoute(path,startPosition,endPosition);
            this.laststartPosition = startPosition;
            this.lastendPosition = endPosition;
            this.lastVehicle=vehicle;
       }
    }

    @Override
    public GeoRoute obtainShortestRouteBetween(GeoPoint startPosition, GeoPoint endPosition, String vehicle)  {
        if(startPosition.equals(endPosition)) {
            return new GeoRoute(startPosition, endPosition);
        }
        try {
            calculateRoute(startPosition, endPosition, vehicle);
        } catch (Exception e) {
            this.lastroute = new GeoRoute(startPosition, endPosition);
            this.laststartPosition = startPosition;
            this.lastendPosition = endPosition;
            this.lastVehicle=vehicle;
        }
        return this.lastroute;
    }

    @Override
    public double estimateDistance(GeoPoint startPosition, GeoPoint endPosition, String vehicle)  {
        if(startPosition.equals(endPosition)) {
            return 0;
        }
        try {
            calculateRoute(startPosition, endPosition, vehicle);
        } catch (Exception e) {
            this.lastroute = new GeoRoute(startPosition, endPosition);
            this.laststartPosition = startPosition;
            this.lastendPosition = endPosition;
            this.lastVehicle=vehicle;
        }
        return this.lastroute.getTotalDistance();
    }
    
}

