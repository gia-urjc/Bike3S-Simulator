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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DEMAND_cost_simple")
public class RecommendationSystemDemandProbabilityCostSimple extends RecommendationSystemDemandProbabilityBased {

    public class RecommendationParameters {


        private double minimumMarginProbability = 0.001;
        private double desireableProbability = 0.8;
        private double MaxCostValue = 5000 ;
        private double maxStationsToReccomend = 30;

        @Override
        public String toString() {
            return  "desireableProbability"+ desireableProbability+"minimumMarginProbability=" + minimumMarginProbability +   ", MaxCostValue=" + MaxCostValue  + ", maxStationsToReccomend=" + maxStationsToReccomend  ;
        }
     }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCostSimple Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    public RecommendationSystemDemandProbabilityCostSimple(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        scc=new CostCalculatorSimple(parameters.minimumMarginProbability, 
                parameters.MaxCostValue, 
                straightLineWalkingVelocity, 
                straightLineCyclingVelocity, 
                probutils, 0, 0);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake() > 0) {
                if (sd.getProbabilityTake() > this.parameters.desireableProbability && sd.getWalkdist() <= maxdistance) {
                    goodfound = true;
                }
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double cost = scc.calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());
                sd.setTotalCost(cost);
                addrent(sd, orderedlist, maxdistance);
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
                double cost = scc.calculateCostReturnSimple(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());
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
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD,double maxdistance) {
        if (newSD.getWalkdist() <= maxdistance && oldSD.getWalkdist() > maxdistance) {
            return true;
        } else if (newSD.getWalkdist() > maxdistance && oldSD.getWalkdist() <= maxdistance) {
            return false;
        } else {
            return (newSD.getTotalCost() < oldSD.getTotalCost());
        }
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        return (newSD.getTotalCost() < oldSD.getTotalCost()); 
    }

}
