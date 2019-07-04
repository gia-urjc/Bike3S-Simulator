/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import static es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes.UtilitiesGlobalLocalUtilityMethods.getOpenSquaredUtility;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;

import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
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

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;

        /**
         * It is the maximum distance in meters between a station and the
         * stations we take into account for checking the area
         */
        private double MaxDistanceSurroundingStations = 500;
        private int MaxDistanceNormalizer=600;
        private double wheightDistanceStationUtility = 0.35;

        @Override
        public String toString() {
            return "maxDistanceRecommendation=" + maxDistanceRecommendation + ", MaxDistanceSurroundingStations=" + MaxDistanceSurroundingStations + ", MaxDistanceNormalizer=" + MaxDistanceNormalizer + ", wheightDistanceStationUtility=" + wheightDistanceStationUtility ;
        }

    }
    public String getParameterString(){
        return "RecommendetionSystemDemandGlobalSurroundingDistanceOpenfunction Parameters{"+ this.parameters.toString() + "}";
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
        super(ss);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
        recutils=new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }
            Comparator<StationUtilityData> DescUtility = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Recommendation> result;
        List<Station> stations = validStationsToRentBike(infrastructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, point, true);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, true);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            result = new ArrayList<>();
            System.out.println("no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result= new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, false);
            List<StationUtilityData> temp = su.stream().sorted(DescUtility).collect(Collectors.toList());
            if (printHints) printRecomendations(temp, false);
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
        } else {
            System.out.println("no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
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
        double currentglobalbikedemand=recutils.dm.getGlobalTakeRatePerHour(current);
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);
            List<Station> otherstations = infrastructureManager.consultStations().stream()
                .filter(other -> s.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());

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
                   * (surbikedemand/ currentglobalbikedemand) * infrastructureManager.getNumberStations();
//                    * (idealbikes/ ud.maxdemand) ;

            double dist = point.distanceTo(s.getPosition());
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
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.dm.getStationTakeRatePerHour(other.getId(),current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingSlotDemand(Station candidatestation,List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current=SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.dm.getStationReturnRatePerHour(other.getId(),current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingOcupation(Station candidatestation,List<Station> otherstations) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = other.availableBikes() * factor;
            accocc += multiplication;
        }
        return accocc;
    }
    private double getSurroundingCapacity(Station candidatestation,List<Station> otherstations) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = other.getCapacity() * factor;
            accocc += multiplication;
        }
        return accocc;
    }
 }
