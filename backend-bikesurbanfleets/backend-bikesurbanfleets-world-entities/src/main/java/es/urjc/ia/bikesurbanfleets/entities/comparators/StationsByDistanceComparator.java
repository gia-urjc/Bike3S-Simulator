package es.urjc.ia.bikesurbanfleets.entities.comparators;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.entities.Station;

import java.util.Comparator;

/**
 * This comparator order stations given a geographical point by distances to this point
 */
public class StationsByDistanceComparator implements Comparator<Station> {


    private GraphManager graph;

    private boolean linearDistance;

    private GeoPoint referencePoint;

    public StationsByDistanceComparator(GraphManager graph, boolean linearDistance, GeoPoint referencePoint) {
        this.graph = graph;
        this.linearDistance = linearDistance;
        this.referencePoint = referencePoint;
    }

    @Override
    public int compare(Station s1, Station s2) {

        double distance1, distance2;
        if (linearDistance) {
            distance1 = s1.getPosition().distanceTo(referencePoint);
            distance2 = s2.getPosition().distanceTo(referencePoint);
        }
        else {
            distance1 = Double.MAX_VALUE;
            distance2 = Double.MIN_VALUE;
            try {
                distance1 = graph.obtainShortestRouteBetween(s1.getPosition(), referencePoint).getTotalDistance();
                distance2 = graph.obtainShortestRouteBetween(s2.getPosition(), referencePoint).getTotalDistance();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
        }
        return Double.compare(distance1, distance2);
    }


}
