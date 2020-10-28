package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import java.util.Comparator;
import java.util.stream.Stream;

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

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {

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
        this.parameters = (RecommendationParameters) (super.parameters);
    }

    @Override
    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist) {
        return candidates
                .filter(stationdata -> stationdata.availableBikes > 0)
                .sorted(byAvailableBikes());
    }

    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        return candidates
                .filter(stationdata -> stationdata.availableSlots > 0)
                .sorted(byAvailableSlots());
    }

    private Comparator<StationData> byAvailableBikes() {
        return (s1, s2) -> {
            int i = Double.compare(s1.availableBikes, s2.availableBikes);
            if (i < 0) {
                return +1;
            }
            if (i > 0) {
                return -1;
            }
            return Double.compare(
                    s1.walkdist, s2.walkdist);
        };
    }

    private Comparator<StationData> byAvailableSlots() {
        return (s1, s2) -> {
            if (s1.walkdist <= this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist > this.parameters.maxDistanceRecommendationReturn) {
                return -1;
            } else if (s1.walkdist > this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist <= this.parameters.maxDistanceRecommendationReturn) {
                return +1;
            } else if (s1.walkdist > this.parameters.maxDistanceRecommendationReturn
                    && s2.walkdist > this.parameters.maxDistanceRecommendationReturn) {
                return Double.compare(
                        s1.walkdist, s2.walkdist);
            } else {
                int i = Double.compare(s1.availableSlots, s2.availableSlots);
                if (i < 0) {
                    return +1;
                }
                if (i > 0) {
                    return -1;
                }
                return Double.compare(
                        s1.walkdist, s2.walkdist);
            }
        };
    }
}
