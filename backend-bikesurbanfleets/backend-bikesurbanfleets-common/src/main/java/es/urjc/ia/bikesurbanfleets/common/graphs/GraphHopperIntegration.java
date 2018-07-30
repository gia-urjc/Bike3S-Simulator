package es.urjc.ia.bikesurbanfleets.common.graphs;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@GraphManagerType("GRAPH_HOPPER")
public class GraphHopperIntegration implements GraphManager {

    @GraphManagerParameters
    public class GraphProperties {

        private String mapDir;

    }

    private final String GRAPHHOPPER_DIR = GlobalInfo.AUX_DIR + "/graphhopper_files";

    private GraphHopper hopper;
    private GHResponse rsp;

    private GeoPoint startPosition;
    private GeoPoint endPosition;

    public GraphHopperIntegration(GraphProperties properties) throws IOException {
        FileUtils.deleteDirectory(new File(GRAPHHOPPER_DIR));
        this.hopper = new GraphHopperOSM().forServer();
        hopper.setDataReaderFile(properties.mapDir);
        hopper.setGraphHopperLocation(GRAPHHOPPER_DIR);
        hopper.setEncodingManager(new EncodingManager("foot"));
        hopper.importOrLoad();

    }

    private GeoRoute responseGHToRoute(PathWrapper path) throws GeoRouteCreationException{
        List<GeoPoint> geoPointList = new ArrayList<>();
        PointList ghPointList = path.getPoints();
        geoPointList.add(startPosition);
        GHPoint3D prev = null;
        for(GHPoint3D p: ghPointList) {
            if(prev != null && p.equals(prev)){
                continue;
            }
            geoPointList.add(new GeoPoint(p.getLat(), p.getLon()));
            prev = p;
        }
        geoPointList.add(endPosition);
        GeoRoute route = new GeoRoute(geoPointList);
        return route;
    }


    private void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperIntegrationException  {
        if(!startPosition.equals(this.startPosition) || !endPosition.equals(this.endPosition)) {
            GHRequest req = new GHRequest(
                    startPosition.getLatitude(), startPosition.getLongitude(),
                    endPosition.getLatitude(), endPosition.getLongitude())
                    .setWeighting("fastest")
                    .setVehicle("foot");
            GHResponse rsp = hopper.route(req);

            if(rsp.hasErrors()) {
                for(Throwable exception: rsp.getErrors()) {
                    throw new GraphHopperIntegrationException(exception.getMessage());
                }
            }
            this.rsp = rsp;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    @Override
    public GeoRoute obtainShortestRouteBetween(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperIntegrationException, GeoRouteCreationException {
        if(startPosition.equals(endPosition)) {
            return new GeoRoute(Arrays.asList(startPosition, endPosition));
        }
        calculateRoutes(startPosition, endPosition);
        PathWrapper path = rsp.getBest();
        return responseGHToRoute(path);
    }

    @Override
    public List<GeoRoute> obtainAllRoutesBetween(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperIntegrationException, GeoRouteCreationException {
        if(startPosition.equals(endPosition)) {
            List<GeoPoint> pointsNewRoute = new ArrayList<>(Arrays.asList(startPosition, endPosition));
            List<GeoRoute> newRoutes = new ArrayList<>();
            GeoRoute newRoute = new GeoRoute(pointsNewRoute);
            newRoutes.add(newRoute);
            return newRoutes;
        }
        calculateRoutes(startPosition, endPosition);
        List<GeoRoute> routes = new ArrayList<>();
        for(PathWrapper p: rsp.getAll()) {
            routes.add(responseGHToRoute(p));
        }
        return routes;
    }

    @Override
    public boolean hasAlternativesRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperIntegrationException {
        calculateRoutes(startPosition, endPosition);
        return rsp.hasAlternatives();
    }

}

