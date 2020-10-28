/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better.UtilitiesGlobalLocalUtilityMethods.getOpenSquaredUtility;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.AbstractRecommendationSystemUtilitiesWithDistanceBased;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_LOCAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION")
public class RecommendetionSystemDemandLocalSurroundingDistanceOpenfunction extends AbstractRecommendationSystemUtilitiesWithDistanceBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemUtilitiesWithDistanceBased.RecommendationParameters {

        /**
         * It is the maximum distance in meters between a station and the
         * stations we take into account for checking the area
         */
        private double MaxDistanceSurroundingStations = 500;
        private int MaxDistanceNormalizer = 600;
        private double wheightDistanceStationUtility = 0.35;
    }

    private RecommendationParameters parameters;
    private UtilitiesGlobalLocalUtilityMethods recutils;

    public RecommendetionSystemDemandLocalSurroundingDistanceOpenfunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        recutils = new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }

    @Override
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction.staticPrintRecomendationDetails(su, rentbike, maxnumber);
    }

    @Override
    public void getStationUtility(StationData sd, boolean rentbike) {
        Station s = sd.station;
        List<Station> otherstations = stationManager.consultStations().stream()
                .filter(other -> s.getPosition().eucleadeanDistanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());

        double surbikedemand = getSurroundingBikeDemand(s, otherstations);
        double surcapacity = getSurroundingCapacity(s, otherstations);
        double surslotdemand = getSurroundingSlotDemand(s, otherstations);
        double surocupation = getSurroundingOcupation(s, otherstations);

        double utility = getOpenSquaredUtility(surcapacity, surocupation, surbikedemand, surslotdemand);
        double newutility;
        if (rentbike) {
            newutility = getOpenSquaredUtility(surcapacity, surocupation - 1, surbikedemand, surslotdemand);
        } else {//return bike 
            newutility = getOpenSquaredUtility(surcapacity, surocupation + 1, surbikedemand, surslotdemand);
        }

        double dist = sd.walkdist;
        double norm_distance = 1 - (dist / parameters.MaxDistanceNormalizer);
        double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                + (1 - parameters.wheightDistanceStationUtility) * ((newutility - utility));
        sd.Utility = globalutility;
        sd.maxopimalocupation = (surcapacity - surslotdemand);
        sd.minoptimalocupation = surbikedemand;
        sd.surroundingCapacity = surcapacity;
        sd.surroundingAvBikes = surocupation;
        if (surbikedemand > (surcapacity - surslotdemand)) {
            sd.optimalocupation = ((surbikedemand + (surcapacity - surslotdemand)) / 2D);
        } else {
            sd.optimalocupation = (Double.NaN);
        }
    }

    private double getSurroundingBikeDemand(Station candidatestation, List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.dm.getStationTakeRatePerHour(other.getId(), current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingSlotDemand(Station candidatestation, List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.dm.getStationReturnRatePerHour(other.getId(), current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingOcupation(Station candidatestation, List<Station> otherstations) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = other.availableBikes() * factor;
            accocc += multiplication;
        }
        return accocc;
    }

    private double getSurroundingCapacity(Station candidatestation, List<Station> otherstations) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = other.getCapacity() * factor;
            accocc += multiplication;
        }
        return accocc;
    }

}
