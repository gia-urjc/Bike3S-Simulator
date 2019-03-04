package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.demandBased;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_PROBABILITY_notexpected_notcompromised")
public class RecommendationSystemDemandProbabilityNENC extends RecommendationSystemDemandProbabilityEC {

    /**
     *
     * @param recomenderdef
     * @param ss
     * @throws Exception
     */
    public RecommendationSystemDemandProbabilityNENC(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(recomenderdef,ss);
        takeintoaccountcompromised=false;
        takeintoaccountexpected=false;
    }
}
