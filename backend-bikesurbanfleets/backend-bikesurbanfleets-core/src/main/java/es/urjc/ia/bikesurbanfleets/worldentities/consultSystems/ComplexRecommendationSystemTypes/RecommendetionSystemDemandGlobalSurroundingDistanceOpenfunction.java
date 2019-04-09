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
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;

import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_GLOBAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION_FUTURE")
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
        private double MaxDistanceSurroundingStations = 600;

        private double wheightDistanceStationUtility = 0.3;

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
    boolean printHints=false;
    private UtilitiesForRecommendationSystems recutils;

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
        recutils=new UtilitiesForRecommendationSystems(this);
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Recommendation> result;
        List<Station> stations = validStationsToRentBike(infrastructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, point, true);
            Comparator<StationUtilityData> DescUtility = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());
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
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().
                filter(station -> station.getPosition().distanceTo(destination) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, false);
            Comparator<StationUtilityData> byDescUtilityIncrement = (sq1, sq2) -> Double.compare(sq2.getUtility(), sq1.getUtility());
            List<StationUtilityData> temp = su.stream().sorted(byDescUtilityIncrement).collect(Collectors.toList());
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
        double currentglobalbikedemand=recutils.getCurrentGlobalBikeDemand();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);
            List<Station> otherstations = infrastructureManager.consultStations().stream()
                .filter(other -> s.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());

            double suridealbikes = getSurroundingIdealBikes(s,otherstations);
            double surcapacity=getSurroundingCapacity(s,otherstations);
            double surmaxidealbikes = surcapacity - getSurroundingIdealSlots(s,otherstations);
            double surocupation = getSurroundingOcupation(s,otherstations);

            double utility = getUtility( 0, suridealbikes, surmaxidealbikes,surcapacity, surocupation );
            double newutility;
            if (rentbike) {
                newutility = getUtility( -1, suridealbikes, surmaxidealbikes,surcapacity, surocupation );
            } else {//return bike 
                newutility = getUtility( +1, suridealbikes, surmaxidealbikes,surcapacity, surocupation );
            }
            double normedUtilityDiff = (newutility - utility)
                   * (suridealbikes/ currentglobalbikedemand) * infrastructureManager.getNumberStations();
//                    * (idealbikes/ ud.maxdemand) ;

            double dist = point.distanceTo(s.getPosition());
            double norm_distance = 1 - normatizeTo01(dist, 0, parameters.maxDistanceRecommendation);
            double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                    + (1 - parameters.wheightDistanceStationUtility) * (newutility - utility);

            /*       double mincap=(double)infraestructureManager.getMinStationCapacity();
            double maxinc=(4D*(mincap-1))/Math.pow(mincap,2);
            double auxnormutil=((newutility-utility+maxinc)/(2*maxinc));
            double globalutility= dist/auxnormutil; 
             */
            sd.setUtility(globalutility);
            sd.setMaxopimalocupation(surmaxidealbikes);
            sd.setMinoptimalocupation(suridealbikes);
            sd.setCapacity(surcapacity);
            sd.setAvailableBikes(surocupation);
            if (suridealbikes > surmaxidealbikes) {
                sd.setOptimalocupation((suridealbikes + surmaxidealbikes) / 2D);
            } else {
                sd.setOptimalocupation(Double.NaN);
            }
            sd.setWalkdist(dist);
            temp.add(sd);
        }
        return temp;
    }

    private double getSurroundingIdealBikes(Station candidatestation,List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.getCurrentBikeDemand(other) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    private double getSurroundingIdealSlots(Station candidatestation,List<Station> otherstations) {
        double accideal = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
            multiplication = recutils.getCurrentSlotDemand(other) * factor;
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

    private double getUtility(int bikeincrement, double idealbikes, double maxidealbikes, double capacity, double avbikes ) {
        double ocupation = avbikes + bikeincrement;
        if (idealbikes <= maxidealbikes) {
            if (ocupation <= idealbikes) {
                return 1 - Math.pow(((ocupation - idealbikes) / idealbikes), 2);
            } else if (ocupation >= maxidealbikes) {
                return 1 - Math.pow(((ocupation - maxidealbikes) / (capacity - maxidealbikes)), 2);
            } else {//if ocupation is just between max and min
                return 1;
            }
        } else { //idealbikes > max idealbikes
            double bestocupation = (idealbikes + maxidealbikes) / 2D;
            if (ocupation <= bestocupation) {
                return 1 - Math.pow(((ocupation - bestocupation) / bestocupation), 2);
            } else {
                double aux = capacity - bestocupation;
                return 1 - Math.pow(((ocupation - bestocupation) / aux), 2);
            }

        }
    }

 }
