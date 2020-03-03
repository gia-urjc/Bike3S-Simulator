package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
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

        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double maxDistanceRecommendationTake = 600;
        private double minimumMarginProbability = 0.0001;
        private double minProbBestNeighbourRecommendation = 0;
        private double desireableProbability = 0.8;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRentPenalisation = 6000;
        private double unsucesscostReturnPenalisation = 6000;
        private double AbandonPenalisation = 24000;
        private int PredictionNorm = 0;
        private int predictionWindow = 900;
        private double normmultiplier = 0.5;
        private double alfa=0.5;

        @Override
        public String toString() {
            return  "alfa=" + alfa + ", maxDistanceRecommendationTake=" + maxDistanceRecommendationTake + ", minimumMarginProbability=" + minimumMarginProbability + ", minProbBestNeighbourRecommendation=" + minProbBestNeighbourRecommendation + ", desireableProbability=" + desireableProbability + ", maxStationsToReccomend=" + maxStationsToReccomend + ", unsucesscostRentPenalisation=" + unsucesscostRentPenalisation + ", unsucesscostReturnPenalisation=" + unsucesscostReturnPenalisation + ", AbandonPenalisation=" + AbandonPenalisation + ", PredictionNorm=" + PredictionNorm + ", predictionWindow=" + predictionWindow + ", normmultiplier=" + normmultiplier ;
        }

    }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCostGlobalPrediction Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculator ucc;

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
        ucc = new ComplexCostCalculator(parameters.minimumMarginProbability, parameters.AbandonPenalisation, parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation,
                expWalkingVelocity,
                expCyclingVelocity, parameters.minProbBestNeighbourRecommendation,
                probutils, parameters.PredictionNorm, parameters.normmultiplier,parameters.alfa,
                graphManager);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxuserdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake() > 0) {
                if (sd.getProbabilityTake() > this.parameters.desireableProbability && sd.getWalkdist() <= maxuserdistance) {
                    goodfound = true;
                }
                try {
                    double cost = ucc.calculateCostsRentAtStation(sd, stationdata, this.parameters.predictionWindow, maxuserdistance, this.parameters.maxDistanceRecommendationTake);
                    sd.setTotalCost(cost);
                    addrent(sd, orderedlist, maxuserdistance);
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
            if (sd.getProbabilityReturn() > 0) {
                if (sd.getProbabilityReturn() > this.parameters.desireableProbability) {
                    goodfound = true;
                }
                try {
                    double cost = ucc.calculateCostsReturnAtStation(sd, userdestination, stationdata, this.parameters.predictionWindow, this.parameters.maxDistanceRecommendationTake);
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

    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        /*       if (newSD.getProbabilityTake() >= this.parameters.desireableProbability
                && oldSD.getProbabilityTake() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityTake() < this.parameters.desireableProbability
                && oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
            return false;
        }
         */ return (newSD.getTotalCost() < oldSD.getTotalCost());
    }

    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        /*      if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityReturn() < this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
         */ return newSD.getTotalCost() < oldSD.getTotalCost();
    }
}
