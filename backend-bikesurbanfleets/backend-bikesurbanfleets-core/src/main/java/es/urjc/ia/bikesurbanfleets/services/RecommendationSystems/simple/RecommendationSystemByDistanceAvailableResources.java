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
@RecommendationSystemType("DISTANCE_RESOURCES")
public class RecommendationSystemByDistanceAvailableResources extends RecommendationSystem {

    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        @Override
        public String toString() {
            return "maxDistanceRecommendation=" + maxDistanceRecommendation ;
        }
    }
    public String getParameterString(){
        return "RecommendationSystemByDistanceAvailableResources Parameters{"+ this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;

    public RecommendationSystemByDistanceAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToRentBike(stationManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            temp = stations.stream().sorted(byProportionBetweenDistanceAndBikes(point)).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        
        List<Station> stations = validStationsToReturnBike(stationManager.consultStations()).stream().collect(Collectors.toList());
        if (!stations.isEmpty()) {
            temp = stations.stream().sorted(byProportionBetweenDistanceAndSlots(destination)).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        } 
        return result;
    }

    	public static Comparator<Station> byProportionBetweenDistanceAndBikes(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    (double) s1.availableBikes(),
                                                  s2.getPosition().distanceTo(point)/
                                                    (double) s2.availableBikes());
	}

	public static Comparator<Station> byProportionBetweenDistanceAndSlots(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    (double) s1.availableSlots(),
                                                  s2.getPosition().distanceTo(point)/
                                                    (double) s2.availableSlots());
	}

}