/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.text.Bidi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DEMAND_cost_simple")
public class RecommendationSystemDemandProbabilityCostSimple extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
        private double desireableProbability = 0.8;
        private double MaxCostValue = 6000 ;
     }

    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    public RecommendationSystemDemandProbabilityCostSimple(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
        scc=new CostCalculatorSimple(
                parameters.MaxCostValue, 
                probutils, 0, 0);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
                double cost = scc.calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());
                sd.setTotalCost(cost);
                addrent(sd, orderedlist, maxdistance);
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
                double cost = scc.calculateCostReturnSimple(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());
                sd.setTotalCost(cost);
                addreturn(sd, orderedlist);
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
   */     return (newSD.getTotalCost() < oldSD.getTotalCost());
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
    }
}
