package es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;

import java.util.Comparator;

/**
 * This comparator order stations given a formula and a reference point
 * The parameter for order is distance/availableSlots
 * It's needed a reference point
 *
 */
public class ComparatorByProportionBetweenDistanceAndSlots implements Comparator<Station> {
    private GeoPoint referencePoint;

    public ComparatorByProportionBetweenDistanceAndSlots(GeoPoint referencePoint) {
        this.referencePoint = referencePoint;
    }

    @Override
    public int compare(Station s1, Station s2) {
        double distance1 = s1.getPosition().distanceTo(referencePoint);
        double distance2 = s2.getPosition().distanceTo(referencePoint);
        return Double.compare(distance1/s1.availableSlots(), distance2/s2.availableSlots());
    }

}
