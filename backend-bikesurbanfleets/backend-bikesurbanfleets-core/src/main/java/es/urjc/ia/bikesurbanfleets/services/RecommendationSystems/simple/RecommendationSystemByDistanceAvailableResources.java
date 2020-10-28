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
import java.util.Comparator;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DISTANCE_RESOURCES")
public class RecommendationSystemByDistanceAvailableResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {
    }
    private RecommendationParameters parameters;

    public RecommendationSystemByDistanceAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(stationdata -> stationdata.availableBikes > 0)
                .sorted(byProportionBetweenDistanceAndBikes());
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(stationdata -> stationdata.availableSlots > 0)
                .sorted(byProportionBetweenDistanceAndSlots());
    }
    private Comparator<StationData> byProportionBetweenDistanceAndBikes() {
        return (s1, s2) -> Double.compare(
                s1.walkdist / s1.availableBikes,
                s2.walkdist / s2.availableBikes);
    }

    private Comparator<StationData> byProportionBetweenDistanceAndSlots() {
        return (s1, s2) -> Double.compare(
                s1.walkdist / s1.availableSlots,
                s2.walkdist / s2.availableSlots);
    }
}
