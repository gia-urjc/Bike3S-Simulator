package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
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
@RecommendationSystemType("DEMAND_cost_prediction_simple")
public class RecommendationSystemDemandProbabilityCostGlobalPredictionSimple extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
        private double desireableProbability = 0.8;
        private double MaxCostValue = 6000 ;
        private int PredictionNorm=0;
        private int predictionWindow=900;
        private double normmultiplier=0.5;
    }
    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    public RecommendationSystemDemandProbabilityCostGlobalPredictionSimple(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
        scc=new CostCalculatorSimple(
                parameters.MaxCostValue, 
                probutils, parameters.PredictionNorm, parameters.normmultiplier);
    }


    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            if (sd.getProbabilityTake()> 0) {
                double cost = scc.calculateCostsRentAtStation(sd, this.parameters.predictionWindow);
                sd.setTotalCost(cost);
                addrent(sd, orderedlist, maxdistance);
            }
        }
        reorder(orderedlist, true);
        return orderedlist;
    }

        @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            if (sd.getProbabilityReturn()> 0) {
                double cost = scc.calculateCostsReturnAtStation(sd, this.parameters.predictionWindow);
                sd.setTotalCost(cost);
                addreturn(sd, orderedlist);
            }
        }
        reorder(orderedlist, false);
        return orderedlist;
    }

    private void reorder(List<StationUtilityData> list, boolean take){
    /*    double cost1;
        double cost2;
        if (list.size()>1){
            cost1=list.get(0).getIndividualCost();
            cost2=list.get(1).getIndividualCost();
            //if the individual cost of the first is the best leave it first
            if (cost1<= cost2) return;
            else { //the second has better individual cost
                
                cost1=list.get(0).getTakecostdiff();
                cost2=list.get(1).getTakecostdiff();
                double prob2=cost1/(cost1+cost2);
                double rand=Math.random();
                if (rand<prob2){
                    StationUtilityData aux=  list.get(0);
                    list.remove(0);
                    list.add(1, aux);
                }
            }
        }
    */    return;
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
   */     return (newSD.getTotalCost() < oldSD.getTotalCost());
      //  return (newSD.getIndividualCost()+newSD.getTakecostdiff() < oldSD.getIndividualCost()+oldSD.getTakecostdiff());
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
  */     return newSD.getTotalCost() < oldSD.getTotalCost();
     //  return (newSD.getIndividualCost()+newSD.getTakecostdiff() < oldSD.getIndividualCost()+oldSD.getTakecostdiff());
    }

 }
