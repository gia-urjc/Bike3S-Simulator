package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.simpleRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import com.google.gson.JsonObject;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

@RecommendationSystemType("DISTANCE_RATIO")
public class RecommendationSystemByDistanceResourcesRatio extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;

    }

    private RecommendationParameters parameters;

    public RecommendationSystemByDistanceResourcesRatio(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        List<Station> stations = validStationsToRentBike(infrastructureManager.consultStations()).stream()
                .filter(station -> station.getPosition().distanceTo(point) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            Comparator<Station> byDistanceBikesRatio = byProportionBetweenDistanceAndBikeRatio(point);
            temp = stations.stream().sorted(byDistanceBikesRatio).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = validStationsToReturnBike(infrastructureManager.consultStations()).stream().collect(Collectors.toList());
        if (!stations.isEmpty()) {
            Comparator<Station> byDistanceSlotsRatio = byProportionBetweenDistanceAndSlotRatio(destination);
            temp = stations.stream().sorted(byDistanceSlotsRatio).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        } 
        return result;
    }
	public static Comparator<Station> byProportionBetweenDistanceAndBikeRatio(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    ((double)s1.availableBikes()/(double)s1.getCapacity()),
                                                  s2.getPosition().distanceTo(point)/
                                                    ((double)s2.availableBikes()/(double)s2.getCapacity()));
	}

	public static Comparator<Station> byProportionBetweenDistanceAndSlotRatio(GeoPoint point) {
		return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point)/
                                                    ((double)s1.availableSlots()/(double)s1.getCapacity()),
                                                  s2.getPosition().distanceTo(point)/
                                                    ((double)s2.availableSlots()/(double)s2.getCapacity()));
	}

    
}
