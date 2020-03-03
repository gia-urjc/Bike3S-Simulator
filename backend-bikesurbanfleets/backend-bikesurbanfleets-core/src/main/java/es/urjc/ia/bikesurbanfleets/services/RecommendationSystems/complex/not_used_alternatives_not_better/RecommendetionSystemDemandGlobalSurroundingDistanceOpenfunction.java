/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better.UtilitiesGlobalLocalUtilityMethods.getOpenSquaredUtility;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_GLOBAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION")
public class RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters{
        /**
         * It is the maximum distance in meters between a station and the
         * stations we take into account for checking the area
         */
        private double MaxDistanceSurroundingStations = 500;
        private int MaxDistanceNormalizer=600;
        private double wheightDistanceStationUtility = 0.35;
    }

    private class StationSurroundingData {

        StationSurroundingData(Station s, double q, double d) {
            station = s;
            quality = q;
            distance = d;
        }

        Station station = null;
        double quality = 0.0D;
        double distance = 0.0D;
    }

    private RecommendationParameters parameters;
    private UtilitiesGlobalLocalUtilityMethods recutils;

    public RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
        recutils=new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }
            Comparator<StationUtilityData> DescUtility = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());

    @Override
   public List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist) {
        List<Recommendation> result;
        List<Station> candidatestations = stationsWithBikesInWalkingDistance( point,  maxdist);

        if (!candidatestations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(candidatestations, point, true);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, true);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>(0);
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result;
        List<Station> stations = stationsWithSlots();

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, false);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, false);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
           result = new ArrayList<>(0);
         }
        return result;
    }
    private void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
        int max = su.size();//Math.min(5, su.size());
        System.out.println();
        if (take) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime());
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        for (int i = 0; i < max; i++) {
            StationUtilityData s = su.get(i);
            System.out.format("Station %3d %2d %2d %10.2f %9.8f %6f %6f %6f %9.8f %9.8f %n", +s.getStation().getId(),
                    s.getStation().availableBikes(),
                    s.getStation().getCapacity(),
                    s.getWalkdist(),
                    s.getUtility(),
                    s.getMinoptimalocupation() ,
                    s.getOptimalocupation() ,
                    s.getMaxopimalocupation(),
                    s.getAvailableBikes(),
                    s.getCapacity() );
        }
        }
    }

    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint point, boolean rentbike) {
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        double currentglobalbikedemand=recutils.getDemandManager().getGlobalTakeRatePerHour(current);
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);
            List<Station> otherstations = stationManager.consultStations().stream()
                .filter(other -> s.getPosition().eucleadeanDistanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());

            double surbikedemand = getSurroundingBikeDemand(s,otherstations);
            double surcapacity=getSurroundingCapacity(s,otherstations);
            double surslotdemand = getSurroundingSlotDemand(s,otherstations);
            double surocupation = getSurroundingOcupation(s,otherstations);

            double utility = getOpenSquaredUtility(surcapacity, surocupation, surbikedemand, surslotdemand);
            double newutility;
            if (rentbike) {
                newutility = getOpenSquaredUtility(surcapacity, surocupation-1, surbikedemand, surslotdemand);
            } else {//return bike 
                newutility = getOpenSquaredUtility(surcapacity, surocupation+1, surbikedemand, surslotdemand);
            }

            double normedUtilityDiff = (newutility - utility)
                   * (surbikedemand/ currentglobalbikedemand) * stationManager.getNumberStations();
//                    * (idealbikes/ ud.maxdemand) ;

            double dist = graphManager.estimateDistance(s.getPosition(),point ,"foot");
            double norm_distance=1-(dist / parameters.MaxDistanceNormalizer);
            double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                    + (1 - parameters.wheightDistanceStationUtility) * (normedUtilityDiff);
            sd.setUtility(globalutility);
            sd.setMaxopimalocupation(surcapacity-surslotdemand);
            sd.setMinoptimalocupation(surbikedemand);
            sd.setCapacity(surcapacity);
            sd.setAvailableBikes(surocupation);
            if (surbikedemand > (surcapacity-surslotdemand)) {
                sd.setOptimalocupation((surbikedemand + (surcapacity-surslotdemand)) / 2D);
            } else {
                sd.setOptimalocupation(Double.NaN);
            }
            sd.setWalkdist(dist);
            temp.add(sd);
        }
        return temp;
    }

    private double getSurroundingBikeDemand(Station candidatestation,List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.getDemandManager().getStationTakeRatePerHour(other.getId(),current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingSlotDemand(Station candidatestation,List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.dm.getStationReturnRatePerHour(other.getId(),current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingOcupation(Station candidatestation,List<Station> otherstations) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = other.availableBikes() * factor;
            accocc += multiplication;
        }
        return accocc;
    }
    private double getSurroundingCapacity(Station candidatestation,List<Station> otherstations) {
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
