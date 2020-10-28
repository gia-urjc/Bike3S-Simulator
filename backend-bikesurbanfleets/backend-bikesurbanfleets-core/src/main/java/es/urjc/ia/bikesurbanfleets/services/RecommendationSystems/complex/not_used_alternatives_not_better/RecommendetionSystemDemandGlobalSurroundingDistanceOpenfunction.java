/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better.UtilitiesGlobalLocalUtilityMethods.getOpenSquaredUtility;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.AbstractRecommendationSystemUtilitiesWithDistanceBased;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_GLOBAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION")
public class RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction extends AbstractRecommendationSystemUtilitiesWithDistanceBased {

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

    public RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        recutils = new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }

    @Override
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        staticPrintRecomendationDetails(su, rentbike, maxnumber);
    }

    protected static void staticPrintRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        if (rentbike) {
            System.out.println("             id av ca    wtime    utility optOcup minOOcup maxOOcup surAvB surCap");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %9.8f %9.8f %9.8f %9.8f %9.8f %9.8f %n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.Utility,
                        s.optimalocupation,
                        s.minoptimalocupation,
                        s.maxopimalocupation,
                        s.surroundingAvBikes,
                        s.surroundingCapacity);
                i++;
            }
        } else {
            System.out.println("             id av ca    wtime    btime    utility optOcup minOOcup maxOOcup surAvB surCap");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f %9.8f %9.8f %9.8f %9.8f  %9.8f %9.8f%n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.biketime,
                        s.Utility,
                        s.optimalocupation,
                        s.minoptimalocupation,
                        s.maxopimalocupation,
                        s.surroundingAvBikes,
                        s.surroundingCapacity);
                i++;
            }
        }
    }

    @Override
    public void getStationUtility(StationData sd, boolean rentbike) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        double currentglobalbikedemand = recutils.getDemandManager().getGlobalTakeRatePerHour(current);
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

        double normedUtilityDiff = (newutility - utility)
                * (surbikedemand / currentglobalbikedemand) * stationManager.getNumberStations();
//                    * (idealbikes/ ud.maxdemand) ;

        double dist = sd.walkdist;
        double norm_distance = 1 - (dist / parameters.MaxDistanceNormalizer);
        double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                + (1 - parameters.wheightDistanceStationUtility) * (normedUtilityDiff);
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
            multiplication = recutils.getDemandManager().getStationTakeRatePerHour(other.getId(), current) * factor;
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
