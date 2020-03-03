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

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
        private double minimumMarginProbability = 0.0001;
        private double minProbBestNeighbourRecommendation = 0;
        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 6000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 6000; //with calculator2bis=between 4000 and 6000
        private double AbandonPenalisation = 24000; //with calculator2bis=0
        private double alfa=0.5;
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculator ucc;

    public RecommendationSystemDemandProbabilityCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);

        ucc = new ComplexCostCalculator(parameters.minimumMarginProbability, parameters.AbandonPenalisation, parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity, parameters.minProbBestNeighbourRecommendation,
                probutils, 0, 0, parameters.alfa, graphManager);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            if (sd.getProbabilityTake() > 0) {
                try {
                    double cost = ucc.calculateCostRentHeuristicNow(sd, stationdata, maxdistance);                  
                    sd.setTotalCost(cost);
                    addrent(sd, orderedlist, maxdistance);
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
        for (StationUtilityData sd : stationdata) {
            if (sd.getProbabilityReturn() > 0) {
                try {
                    double cost = ucc.calculateCostReturnHeuristicNow(sd, userdestination, stationdata);
                    sd.setTotalCost(cost);
                    addreturn(sd, orderedlist);
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
