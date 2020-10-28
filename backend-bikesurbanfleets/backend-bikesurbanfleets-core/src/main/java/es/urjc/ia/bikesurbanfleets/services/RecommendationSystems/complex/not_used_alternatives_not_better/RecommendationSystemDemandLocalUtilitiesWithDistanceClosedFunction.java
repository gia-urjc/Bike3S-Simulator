package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.AbstractRecommendationSystemUtilitiesWithDistanceBased;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import java.time.LocalDateTime;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("LOCAL_UTILITY_W_DISTANCE_DEMAND_CLOSEDFUNCTION")
public class RecommendationSystemDemandLocalUtilitiesWithDistanceClosedFunction extends AbstractRecommendationSystemUtilitiesWithDistanceBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemUtilitiesWithDistanceBased.RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int MaxDistanceNormalizer = 600;
        private double wheightDistanceStationUtility = 0.35;
    }

    private RecommendationParameters parameters;
    private UtilitiesGlobalLocalUtilityMethods recutils;

    public RecommendationSystemDemandLocalUtilitiesWithDistanceClosedFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        recutils = new UtilitiesGlobalLocalUtilityMethods(getDemandManager());
    }

    @Override
    public void getStationUtility(StationData sd, boolean rentbike) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        double idealAvailable = (recutils.getDemandManager().getStationTakeRatePerHour(sd.station.getId(), current)
                + (sd.station.getCapacity() - recutils.getDemandManager().getStationReturnRatePerHour(sd.station.getId(), current))) / 2D;
        double utildif = recutils.calculateClosedSquaredStationUtilityDifferencewithDemand(sd.station, rentbike);
        double dist = sd.walkdist;
        double norm_distance = 1 - (dist / parameters.MaxDistanceNormalizer);
        double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                + (1 - parameters.wheightDistanceStationUtility) * utildif;

        sd.Utility = globalutility;
        sd.optimalocupation = idealAvailable;
    }
}
