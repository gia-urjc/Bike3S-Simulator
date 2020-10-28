package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better.UtilitiesGlobalLocalUtilityMethods;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("GLOBAL_UTILITY_W_DISTANCE")
public class RecommendationSystemGlobalUtilitiesWithDistance extends AbstractRecommendationSystemUtilitiesWithDistanceBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemUtilitiesWithDistanceBased.RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int MaxDistanceNormalizer = 600;
        private double wheightDistanceStationUtility = 0.35;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemGlobalUtilitiesWithDistance(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
    }
 
    @Override
    public void getStationUtility(StationData s, boolean rentbike) {
        double idealAvailable = s.station.getCapacity() / 2D;
        double utildif = UtilitiesGlobalLocalUtilityMethods.calculateClosedSquaredStationUtilityDifferencewithoutDemand(s.station, rentbike);
        double normedUtilityDiff = utildif * ((double) s.station.getCapacity() / (double) stationManager.getMaxStationCapacity());
        double maxcap = stationManager.getMaxStationCapacity();
        double utilitymax = (maxcap - 1) * 4 / (maxcap * maxcap);
        double utilitynorm = (normedUtilityDiff + utilitymax) / (2 * utilitymax);
        double dist = s.walkdist;
        double norm_distance = 1 - (dist / parameters.MaxDistanceNormalizer);
        double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                + (1 - parameters.wheightDistanceStationUtility) * (utilitynorm);
        s.Utility = globalutility;
        s.optimalocupation = idealAvailable;
    }
}
