package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

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
@RecommendationSystemType("DEMAND_cost")
public class RecommendationSystemDemandProbabilityCost extends RecommendationSystemDemandProbabilityBased {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;

        private double minimumMarginProbability = 0.001;
        private double minProbBestNeighbourRecommendation = 0.5;
        private double desireableProbability = 0.5;
        private double penalisationfactorrent = 1;
        private double penalisationfactorreturn = 1;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRent = 3000;
        private double unsucesscostReturn = 2000;

        @Override
        public String toString() {
            return "maxDistanceRecommendation=" + maxDistanceRecommendation + ", minimumMarginProbability=" + minimumMarginProbability + ", minProbBestNeighbourRecommendation=" + minProbBestNeighbourRecommendation + ", desireableProbability=" + desireableProbability + ", penalisationfactorrent=" + penalisationfactorrent + ", penalisationfactorreturn=" + penalisationfactorreturn + ", maxStationsToReccomend=" + maxStationsToReccomend + ", unsucesscostRent=" + unsucesscostRent + ", unsucesscostReturn=" + unsucesscostReturn;
        }
    }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCost Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculator2 ucc;

    public RecommendationSystemDemandProbabilityCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(recomenderdef, ss);
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
        ucc = new ComplexCostCalculator2(parameters.minimumMarginProbability, parameters.unsucesscostRent,
                parameters.unsucesscostReturn,
                parameters.penalisationfactorrent, parameters.penalisationfactorreturn, straightLineWalkingVelocity, 
                straightLineCyclingVelocity, parameters.minProbBestNeighbourRecommendation,
                parameters.maxDistanceRecommendation);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake() > 0) {
                if (sd.getProbabilityTake() > this.parameters.desireableProbability && sd.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
                    goodfound = true;
                }
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double cost = ucc.calculateCostRentHeuristic(sd, 1, sd.getWalkTime(), lookedlist, stationdata, true);
                sd.setTotalCost(cost);
                addrent(sd, orderedlist);
                if (goodfound) {
                    i++;
                }
            }
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityReturn() > 0) {
                if (sd.getProbabilityReturn() > this.parameters.desireableProbability) {
                    goodfound = true;
                }
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double biketime = sd.getBiketime();
                double cost = ucc.calculateCostReturnHeuristic(sd, 1, biketime, userdestination, lookedlist, stationdata, true);
                sd.setTotalCost(cost);
                addreturn(sd, orderedlist);
                if (goodfound) {
                    i++;
                }
            }
        }
        return orderedlist;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
/*        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability
                    && newSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return betterOrSameDecideSimilar(newSD, oldSD);
            }
            if (newSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return true;
            }
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return false;
            }
            if (oldSD.getProbabilityTake() >= newSD.getProbabilityTake()) {
                return false;
            }
            return true;
        }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return true;
        }
*/        return betterOrSameDecideSimilar(newSD, oldSD);
    }

    protected boolean betterOrSameDecideSimilar(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getTotalCost() < oldSD.getTotalCost()) {
            return true;
        } else {
            return false;
        }
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
/*        if (oldSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && newSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return betterOrSameDecideSimilar(newSD, oldSD);
        }
        if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return true;
        }
        if (oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
*/        return betterOrSameDecideSimilar(newSD, oldSD);
    }

}
