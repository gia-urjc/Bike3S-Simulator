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
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("AVAILABLE_RESOURCES")
public class RecommendationSystemByAvailableResources extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters{
        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendationReturn = 600;
    }
    private RecommendationParameters parameters;

    public RecommendationSystemByAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
    }

    @Override
   public List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithBikesInWalkingDistance( point,  maxdist);

        if (!candidatestations.isEmpty()) {
            Comparator<Station> byBikes = byAvailableBikes(point);
            temp = candidatestations.stream().sorted(byBikes).collect(Collectors.toList());
            result = temp.stream().map(station -> new Recommendation(station, null)).collect(Collectors.toList());
        }
        return result;
    }

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Station> temp;
        List<Recommendation> result = new ArrayList<>();
        List<Station> candidatestations = stationsWithSlotsInWalkingDistance(destination,parameters.maxDistanceRecommendationReturn);

        if (!candidatestations.isEmpty()) {
            Comparator<Station> bySlots = byAvailableSlots(destination);
            temp = candidatestations.stream().sorted(bySlots).collect(Collectors.toList());
            result = temp.stream().map(s -> new Recommendation(s, null)).collect(Collectors.toList());
        }
        return result;
    }

    public  Comparator<Station> byAvailableBikes(GeoPoint pos) {
        return (s1, s2) -> {
            int i = Integer.compare(s1.availableBikes(), s2.availableBikes());
            if (i < 0) {
                return +1;
            }
            if (i > 0) {
                return -1;
            }
            return Double.compare(
                    graphManager.estimateDistance(pos, s1.getPosition() ,"foot"),
                    graphManager.estimateDistance(pos, s2.getPosition() ,"foot"));
        };
    }

    public  Comparator<Station> byAvailableSlots(GeoPoint pos) {
        return (s1, s2) -> {
            int i = Integer.compare(s1.availableSlots(), s2.availableSlots());
            if (i < 0) {
                return +1;
            }
            if (i > 0) {
                return -1;
            }
            return Double.compare(
                    graphManager.estimateDistance(s1.getPosition(),pos ,"foot"),
                    graphManager.estimateDistance(s2.getPosition(),pos ,"foot"));
        };
    }

}
