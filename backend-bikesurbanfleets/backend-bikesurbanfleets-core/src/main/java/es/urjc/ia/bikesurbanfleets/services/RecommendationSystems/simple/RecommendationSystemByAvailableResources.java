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

    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendationReturn = 600;

        @Override
        public String toString() {
            return "maxDistanceRecommendationReturn=" + maxDistanceRecommendationReturn ;
        }

    }
    public String getParameterString(){
        return "RecommendationSystemByAvailableResources Parameters{"+ this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;

    public RecommendationSystemByAvailableResources(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
