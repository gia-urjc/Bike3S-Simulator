/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author holger
 */
@RecommendationSystemType("SURROUNDING_AVAILABLE_RESOURCES")
public class RecommendationSystemSurroundingByAvailableResources extends RecommendationSystem {

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
        private double MaxDistanceSurroundingStations = 300;

        @Override
        public String toString() {
            return  "maxDistanceRecommendation=" + maxDistanceRecommendation + ", MaxDistanceSurroundingStations=" + MaxDistanceSurroundingStations ;
        }

    }
    public String getParameterString(){
        return "RecommendationSystemSurroundingByAvailableResources Parameters{"+ this.parameters.toString() + "}";
    }

    private class StationSurroundingData {
        StationSurroundingData(Station s, double q, double d, Station n){
            station=s;
            quality=q;
            distance=d;
            nearest=n;
        }

        Station station = null;
        double quality = 0.0D;
        double distance = 0.0D;
        Station nearest=null;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemSurroundingByAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
    }


    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint point) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToRentBike(stationManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityRenting(stations,point);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byQuality(point)).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, true);
            }
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(stationManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(destination) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityReturning(stations, destination);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byQuality(destination)).collect(Collectors.toList());
            if (printHints) {
                printRecomendations(temp, false);
            }
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        } 

        return result;
    }
        private void printRecomendations(List<StationSurroundingData> su, boolean take) {
        if (printHints) {
        int max = su.size();//Math.min(5, su.size());
        System.out.println();
        if (take) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime());
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        for (int i = 0; i < max; i++) {
            StationSurroundingData s = su.get(i);
            if (s.nearest!=null)
            System.out.format("Station %3d %2d %2d %10.2f %10.2f %3d %10.2f %2d%n", +
                    s.station.getId(),
                    s.station.availableBikes(),
                    s.station.getCapacity(),
                    s.distance,
                    s.quality,
                    s.nearest.getId(),
                    s.nearest.getPosition().distanceTo(s.station.getPosition()),
                    s.nearest.availableBikes());
            else 
            System.out.format("Station %3d %2d %2d %10.2f %10.2f %3d %n", +
                    s.station.getId(),
                    s.station.availableBikes(),
                    s.station.getCapacity(),
                    s.distance,
                    s.quality,
                    0);
            }
        }
    }
    private List<StationSurroundingData> getStationQualityRenting(List<Station> stations, GeoPoint pos) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();

        for (Station candidatestation : stations) {
            double summation = 0;
            List<Station> otherstations = stationManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            double nearestdist=Double.MAX_VALUE;
            Station nearest=null;
            for (Station other : otherstations) {
                double dist=candidatestation.getPosition().distanceTo(other.getPosition());
                factor = (parameters.MaxDistanceSurroundingStations - dist) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableBikes() * factor;
                summation += multiplication;
                if (dist<nearestdist && other.getId()!=candidatestation.getId()){
                    nearest=other;
                    nearestdist=dist;
                }
            }
            double dist=candidatestation.getPosition().distanceTo(pos);
            stationdat.add(new StationSurroundingData(candidatestation,summation, dist, nearest));
        }
        return stationdat;
    }
    
    private List<StationSurroundingData> getStationQualityReturning(List<Station> stations, GeoPoint dest) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();

        for (Station candidatestation : stations) {
            double nearestdist=Double.MAX_VALUE;
            Station nearest=null;
           double summation = 0;
            List<Station> otherstations = stationManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            for (Station other : otherstations) {
                double dist=candidatestation.getPosition().distanceTo(other.getPosition());
                factor = (parameters.MaxDistanceSurroundingStations - dist) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableSlots() * factor;
                summation += multiplication;
                if (dist<nearestdist && other.getId()!=candidatestation.getId()){
                    nearest=other;
                    nearestdist=dist;
                }
            }
            double dist=candidatestation.getPosition().distanceTo(dest);
            stationdat.add(new StationSurroundingData(candidatestation,summation,dist, nearest));
        }
        return stationdat;
    }
 
    public static Comparator<StationSurroundingData> byQuality(GeoPoint pos) {
        return (s1, s2) -> {
            int i = Double.compare(s1.quality, s2.quality);
            if (i < 0) {
                return +1;
            }
            if (i > 0) {
                return -1;
            }
            return Double.compare(s1.station.getPosition().distanceTo(pos), s2.station.getPosition().distanceTo(pos));
        };
    }

 }
