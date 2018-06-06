package es.urjc.ia.bikesurbanfleets.entities.comparators;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.entities.Station;

import java.util.Comparator;

/**
 * This comparator order stations given a formula and a reference point
 * The parameter for order is distance/availableSlots
 * It's needed a reference point
 *
 */
public class ComparatorByProportionBetweenDistanceAndSlots implements Comparator<Station> {

    private GraphManager graph;

    private GeoPoint referencePoint;

    public ComparatorByProportionBetweenDistanceAndSlots(GraphManager graph, GeoPoint referencePoint) {
        this.graph = graph;
        this.referencePoint = referencePoint;
    }

    @Override
    public int compare(Station s1, Station s2) {
        double distance1, distance2;
        distance1 = s1.getPosition().distanceTo(referencePoint);
        distance2 = s2.getPosition().distanceTo(referencePoint);

        return Double.compare(distance1/s1.availableSlots(), distance2/s2.availableSlots());
    }

}
