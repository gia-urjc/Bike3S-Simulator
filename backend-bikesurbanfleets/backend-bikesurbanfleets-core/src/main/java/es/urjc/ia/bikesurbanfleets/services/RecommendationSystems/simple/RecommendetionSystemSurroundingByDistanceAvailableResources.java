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
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        private double MaxDistanceSurroundingStations = 300;
    }
    private class StationSurroundingData {
        StationSurroundingData(Station s, double q, double d){
            station=s;
            quality=q;
            distance=d;
        }

        Station station = null;
        double quality = 0.0D;
        double distance = 0.0D;
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
    public List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithBikesInWalkingDistance( point,  maxdist);

        if (!candidatestations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityandDistanceRenting(candidatestations, point);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byProportionBetweenDistanceAndQuality).collect(Collectors.toList());
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithSlots();

        if (!candidatestations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityandDistanceReturning(candidatestations, destination);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byProportionBetweenDistanceAndQuality).collect(Collectors.toList());
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        } 
        return result;
    }

    private List<StationSurroundingData> getStationQualityandDistanceRenting(List<Station> stations, GeoPoint userpoint) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();
        for (Station candidatestation : stations) {
            double summation = 0;
            List<Station> otherstations = stationManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            for (Station other : otherstations) {
                factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableBikes() * factor;
                summation += multiplication;
            }
            double dist=graphManager.estimateDistance(userpoint, candidatestation.getPosition() ,"foot");
            stationdat.add(new StationSurroundingData(candidatestation, summation,dist));
        }
        return stationdat;
    }

    private List<StationSurroundingData> getStationQualityandDistanceReturning(List<Station> stations, GeoPoint userpoint) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();

        for (Station candidatestation : stations) {
            double summation = 0;
            List<Station> otherstations = stationManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            for (Station other : otherstations) {
                factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableSlots() * factor;
                summation += multiplication;
            }
            double dist=graphManager.estimateDistance(candidatestation.getPosition(),userpoint ,"foot");
            stationdat.add(new StationSurroundingData(candidatestation, summation,dist));
        }
        return stationdat;
    }
    
    Comparator<StationSurroundingData> byProportionBetweenDistanceAndQuality = (sq1, sq2) ->  Double.compare(sq1.distance/ sq1.quality, sq2.distance/ sq2.quality);

}
