package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.ComplexCostCalculator3.BetterFirstStationException;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.ArrayList;
import java.util.LinkedList;
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
@RecommendationSystemType("DEMAND_cost_prediction")
public class RecommendationSystemDemandProbabilityCostGlobalPrediction extends RecommendationSystemDemandProbabilityBased {

    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double minimumMarginProbability = 0.001;
        private double minProbBestNeighbourRecommendation = 0.05;
        private double desireableProbability = 0.5;
        private double penalisationfactorrent = 1;
        private double penalisationfactorreturn = 1;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRent = 3000;
        private double unsucesscostReturn = 2000;
        private double MaxCostValue = 5000;
        private boolean squaredTimes = true;
        private int PredictionNorm = 0;
        private int predictionWindow = 1800;

        @Override
        public String toString() {
            return "predictionWindow=" + predictionWindow + ", PredictionNorm=" + PredictionNorm + ", squaredTimes=" + squaredTimes + ", maxDistanceRecommendation=" + maxDistanceRecommendation + ", MaxCostValue=" + MaxCostValue + ", minimumMarginProbability=" + minimumMarginProbability + ", minProbBestNeighbourRecommendation=" + minProbBestNeighbourRecommendation + ", desireableProbability=" + desireableProbability + ", penalisationfactorrent=" + penalisationfactorrent + ", penalisationfactorreturn=" + penalisationfactorreturn + ", maxStationsToReccomend=" + maxStationsToReccomend + ", unsucesscostRent=" + unsucesscostRent + ", unsucesscostReturn=" + unsucesscostReturn;
        }

    }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCostGlobalPrediction Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculator2 ucc;

    public RecommendationSystemDemandProbabilityCostGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        ucc = new ComplexCostCalculator2(parameters.minimumMarginProbability, parameters.MaxCostValue, parameters.unsucesscostRent,
                parameters.unsucesscostReturn,
                parameters.penalisationfactorrent, parameters.penalisationfactorreturn, straightLineWalkingVelocity,
                straightLineCyclingVelocity, parameters.minProbBestNeighbourRecommendation,
                parameters.maxDistanceRecommendation, probutils, parameters.squaredTimes, parameters.PredictionNorm);
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
            sd.setProbabilityTake(probutils.calculateTakeProbability(sd.getStation(), sd.getWalkTime()));
            if (sd.getProbabilityTake() > 0) {
                if (sd.getProbabilityTake() > this.parameters.desireableProbability && sd.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
                    goodfound = true;
                }
                try {
                    double cost = ucc.calculateCostsRentAtStation(sd, stationdata, this.parameters.predictionWindow);
                    sd.setTotalCost(cost);
                    addrent(sd, orderedlist);
                    if (goodfound) {
                        i++;
                    }
                } catch (BetterFirstStationException e) {
                    System.out.println("Better neighbour");

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
            sd.setProbabilityReturn(probutils.calculateReturnProbability(sd.getStation(), sd.getBiketime()));
            if (sd.getProbabilityReturn() > 0) {
                if (sd.getProbabilityReturn() > this.parameters.desireableProbability) {
                    goodfound = true;
                }
                try {
                    double cost = ucc.calculateCostsReturnAtStation(sd, userdestination, stationdata, this.parameters.predictionWindow);
                    sd.setTotalCost(cost);
                    addreturn(sd, orderedlist);
                    if (goodfound) {
                        i++;
                    }
                } catch (BetterFirstStationException e) {
                    System.out.println("Better neighbour");

                }
            }
        }
        return orderedlist;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() > this.parameters.maxDistanceRecommendation) {
            return true;
        } else if (newSD.getWalkdist() > this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        } else {
            if (newSD.getTotalCost() < oldSD.getTotalCost()) {
                return true;
            } else {
                return false;
            }
        }
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getTotalCost() < oldSD.getTotalCost()) {
            return true;
        } else {
            return false;
        }
    }
}
