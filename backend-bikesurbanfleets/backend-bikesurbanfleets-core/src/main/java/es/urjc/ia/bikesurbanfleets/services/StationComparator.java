package es.urjc.ia.bikesurbanfleets.services;

import java.util.Comparator;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

public class StationComparator {

    public StationComparator() {
    }

    public static Comparator<Station> byDistance(GeoPoint point, GraphManager gm, String vehicle) {
        return (s1, s2) -> Double.compare(
                gm.estimateDistance(point, s1.getPosition(), vehicle),
                gm.estimateDistance(point, s2.getPosition(), vehicle)
        );
    }

    public static Comparator<Station> byAvailableBikes() {
        return (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
    }

    public static Comparator<Station> byAvailableSlots() {
        return (s1, s2) -> Integer.compare(s2.availableSlots(), s1.availableSlots());
    }

    public static Comparator<Station> byProportionBetweenDistanceAndBikes(GeoPoint point, GraphManager gm, String vehicle) {
        return (s1, s2) -> Double.compare(
                gm.estimateDistance(point, s1.getPosition(), vehicle)
                / (double) s1.availableBikes(),
                gm.estimateDistance(point, s2.getPosition(), vehicle)
                / (double) s2.availableBikes());
    }

    public static Comparator<Station> byProportionBetweenDistanceAndSlots(GeoPoint point, GraphManager gm, String vehicle) {
        return (s1, s2) -> Double.compare(
                gm.estimateDistance(point, s1.getPosition(), vehicle)
                / (double) s1.availableSlots(),
                gm.estimateDistance(point, s2.getPosition(), vehicle)
                / (double) s2.availableSlots());
    }

}
