package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.not_used_alternatives_not_better;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.AbstractRecommendationSystemUtilitiesWithDistanceBased;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("LOCAL_UTILITY_W_DISTANCE_DEMAND_OPENFUNCTION")
public class RecommendationSystemDemandLocalUtilitiesWithDistanceOpenFunction extends AbstractRecommendationSystemUtilitiesWithDistanceBased {

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

    public RecommendationSystemDemandLocalUtilitiesWithDistanceOpenFunction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        double currentbikedemand = recutils.getDemandManager().getStationTakeRatePerHour(sd.station.getId(), current);
        double currentslotdemand = recutils.getDemandManager().getStationReturnRatePerHour(sd.station.getId(), current);
        double maxidealbikes = sd.station.getCapacity() - currentslotdemand;
        double minidealbikes = currentbikedemand;
        double util = recutils.calculateOpenSquaredStationUtilityDifference(sd, rentbike);
        double dist = sd.walkdist;
        double norm_distance = 1 - (dist / parameters.MaxDistanceNormalizer);
        double globalutility = parameters.wheightDistanceStationUtility * norm_distance
                + (1 - parameters.wheightDistanceStationUtility) * util;

        sd.Utility = globalutility;
        sd.maxopimalocupation = maxidealbikes;
        sd.minoptimalocupation = minidealbikes;
        if (minidealbikes > maxidealbikes) {
            sd.optimalocupation = ((minidealbikes + maxidealbikes) / 2D);
        } else {
            sd.optimalocupation = (Double.NaN);
        }
    }

}
