/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_DISTANCE_RESOURCES")
public class RecommendetionSystemSurroundingByDistanceAvailableResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters{
        /**
         * It is the maximum distance in meters between a station and the
         * stations we take into account for checking the area
         */
        private double MaxDistanceSurroundingStations = 500;
    }

    private RecommendationParameters parameters;

    public RecommendetionSystemSurroundingByDistanceAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
    }

    @Override
    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(s -> s.availableBikes > 0) //filter station candat¡dates
                .map(s -> {
                    getStationQuality(s, true);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(byProportionBetweenDistanceAndQuality()); //sort by utility
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(s -> s.availableSlots > 0) //filter station candat¡dates
                .map(s -> {
                    getStationQuality(s, false);
                    return s;
                })//apply function to calculate utilities the utility data
                .sorted(byProportionBetweenDistanceAndQuality()); //sort by utility
    }

    private void getStationQuality(StationData s, boolean take) {
        double summation = 0;
        double factor, multiplication;
        double nearestdist = Double.MAX_VALUE;
        Station nearest = null;
        for (Station other : stationManager.consultStations()) {
            double dist = s.station.getPosition().eucleadeanDistanceTo(other.getPosition());
            if (dist <= parameters.MaxDistanceSurroundingStations) {
                factor = (parameters.MaxDistanceSurroundingStations - dist) / parameters.MaxDistanceSurroundingStations;
                double slotsorbikes= take ? other.availableBikes() : other.availableSlots();
                multiplication = slotsorbikes * factor;
                summation += multiplication;
                if (dist < nearestdist && other.getId() != s.station.getId()) {
                    nearest = other;
                    nearestdist = dist;
                }
            }
        }
        s.quality = summation;
        s.nearest = nearest;
        s.nearestDistance = nearestdist;
    }
    
    private static Comparator<StationData> byProportionBetweenDistanceAndQuality() {
        return (s1, s2) -> Double.compare(
                s1.walkdist / s1.quality,
                s2.walkdist / s2.quality);
    }
    
    //default implementation for printing details
    @Override
    protected  void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        RecommendationSystemSurroundingByAvailableResources.staticPrintRecomendationDetails(su, rentbike, maxnumber);
    }
}
