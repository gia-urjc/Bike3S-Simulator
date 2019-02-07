/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
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

    }

    private class StationSurroundingData {
        StationSurroundingData(Station s, double q){
            station=s;
            quality=q;
        }

        Station station = null;
        double quality = 0.0D;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemSurroundingByAvailableResources(JsonObject recomenderdef, InfraestructureManager infraestructureManager) throws Exception {
        super(infraestructureManager);
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
        List<Station> stations = validStationsToRentBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityRenting(stations);
            Comparator<StationSurroundingData> byQuality = (sq1, sq2) -> Double.compare(sq2.quality, sq1.quality);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byQuality).collect(Collectors.toList());
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint point) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infraestructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationSurroundingData> stationdata = getStationQualityReturning(stations);
            Comparator<StationSurroundingData> byQuality = (sq1, sq2) -> Double.compare(sq2.quality, sq1.quality);
            List<StationSurroundingData> temp = stationdata.stream().sorted(byQuality).collect(Collectors.toList());
            result = temp.stream().map(StationSurroundingData -> new Recommendation(StationSurroundingData.station, null)).collect(Collectors.toList());
        } else { //if no best station has been found in the max distance
            Comparator<Station> byDistance = StationComparator.byDistance(point);
            List<Station> temp = validStationsToReturnBike(infraestructureManager.consultStations()).stream().sorted(byDistance).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }

        return result;
    }

    private List<StationSurroundingData> getStationQualityRenting(List<Station> stations) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();

        for (Station candidatestation : stations) {
            double summation = 0;
            List<Station> otherstations = infraestructureManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            for (Station other : otherstations) {
                factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableBikes() * factor;
                summation += multiplication;
            }
            stationdat.add(new StationSurroundingData(candidatestation,summation));
        }
        return stationdat;
    }
    
    private List<StationSurroundingData> getStationQualityReturning(List<Station> stations) {
        List<StationSurroundingData> stationdat = new ArrayList<StationSurroundingData>();

        for (Station candidatestation : stations) {
            double summation = 0;
            List<Station> otherstations = infraestructureManager.consultStations().stream()
                    .filter(other -> candidatestation.getPosition().distanceTo(other.getPosition()) <= parameters.MaxDistanceSurroundingStations).collect(Collectors.toList());
            double factor, multiplication;
            for (Station other : otherstations) {
                factor = (parameters.MaxDistanceSurroundingStations - candidatestation.getPosition().distanceTo(other.getPosition())) / parameters.MaxDistanceSurroundingStations;
                multiplication = other.availableSlots() * factor;
                summation += multiplication;
            }
            stationdat.add(new StationSurroundingData(candidatestation,summation));
        }
        return stationdat;
    }
}
