/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DISTANCE_EXPECTED_RESOURCES")
public class RecommendationSystemByDistanceExpectedResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {
    }
    private RecommendationParameters parameters;

    public RecommendationSystemByDistanceExpectedResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithExpectedBikesInWalkingDistance(point, maxdist);

        if (!candidatestations.isEmpty()) {
            temp = candidatestations.stream().sorted(
                    StationComparator.byProportionBetweenDistanceAndBikes(point, graphManager, "foot")
            ).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithExpectedSlotsInBikingDistance(currentposition);
        if (!candidatestations.isEmpty()) {
            temp = candidatestations.stream().sorted(
                    StationComparator.byProportionBetweenDistanceAndSlots(destination, graphManager, "foot")
            ).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }
        return result;
    }

    private List<Station> stationsWithExpectedBikesInWalkingDistance(GeoPoint position, double maxdist) {
        List<Station> temp = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double walkdist = graphManager.estimateDistance(position, s.getPosition(), "foot");
            if (walkdist <= maxdist) {
                double walktime = walkdist / parameters.expectedWalkingVelocity;
                PastRecommendations.ExpBikeChangeResult er = this.pastRecomendations.getExpectedBikechanges(s.getId(), 0, walktime);
                int expbikes = s.availableBikes() + er.changes + er.minpostchanges;
                if (expbikes > 0) {
                    temp.add(s);
                }
            }
        }
        return temp;
    }

    private List<Station> stationsWithExpectedSlotsInBikingDistance(GeoPoint position) {
        List<Station> temp = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double walkdist = graphManager.estimateDistance(position, s.getPosition(), "bike");
            double walktime = walkdist / parameters.expectedWalkingVelocity;
            PastRecommendations.ExpBikeChangeResult er = this.pastRecomendations.getExpectedBikechanges(s.getId(),0, walktime);
            int expslots = s.availableSlots() - er.changes - er.maxpostchanges;
            if (expslots > 0) {
                temp.add(s);
            }
        }

        return temp;
    }

}
