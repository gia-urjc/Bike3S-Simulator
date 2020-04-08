/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.common.util.StationProbabilitiesQueueBased;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculationQueue.IntTuple;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.text.Bidi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DEMAND_cost_surrounding")
public class RecommendationSystemDemandProbabilityCostSurrounding extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double MaxCostValue = 6000;
        private double MaxDistanceSurroundingStations = 500;
        private double alfa=0.1;
    }

    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    private UtilitiesProbabilityCalculationQueue probutilsqueue;

    public RecommendationSystemDemandProbabilityCostSurrounding(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters = (RecommendationParameters) (super.parameters);
        scc = new CostCalculatorSimple(
                parameters.MaxCostValue,
                probutils, 0, 0,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity, 
                graphManager);
        probutilsqueue = (UtilitiesProbabilityCalculationQueue) probutils;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {

            double prob = sd.getProbabilityTake();
            double msdprob = Math.pow(prob, 2.718);
   //         double cost = sd.getWalkTime() + (1 - msdprob) * parameters.MaxCostValue;

            double secprob=getSurroundingProbRent(sd, sd.getWalkdist(), allstations, maxdistance);
            double seccost = secprob*sd.getWalkTime() + (1 - secprob) * parameters.MaxCostValue;

            double cost = sd.getWalkTime() + (1 - msdprob) * 
                    (parameters.alfa* seccost +(1-parameters.alfa)*parameters.MaxCostValue);

            sd.setTotalCost(cost);
            addrent(sd, orderedlist, maxdistance);
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {

            double prob = sd.getProbabilityReturn();
            double msdprob = Math.pow(prob, 2.718);
     //       double cost = sd.getWalkTime() + (1 - msdprob) * parameters.MaxCostValue;

            double secprob=getSurroundingProbReturn(sd, sd.getBikedist(), allstations);
            double seccost = secprob*(sd.getBiketime()+sd.getWalkTime()) + (1 - secprob) * parameters.MaxCostValue;

            double cost = sd.getBiketime() + msdprob * sd.getWalkTime() + (1 - msdprob) * 
                    (parameters.alfa* seccost +(1-parameters.alfa)*parameters.MaxCostValue);
            sd.setTotalCost(cost);
            addreturn(sd, orderedlist);
        }
        return orderedlist;
    }

    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        return (newSD.getTotalCost() < oldSD.getTotalCost());
    }

    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        return newSD.getTotalCost() < oldSD.getTotalCost();
    }

    private double getSurroundingProbRent(StationUtilityData station,
            double accwalkdistance, List<Station> stations, double maxdistance) {
        int suravcap = 0;
        int suravslots = 0;
        int suravbikes = 0;
        int surminpostchanges = 0;
        int surmaxpostchanges = 0;
        double surtakedemandrate = 0;
        double surreturndemandrate = 0;
        boolean found = false;
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            if (s.getId() != station.getStation().getId()) {
                double dist = station.getStation().getPosition().eucleadeanDistanceTo(s.getPosition());
                if ((dist <= parameters.MaxDistanceSurroundingStations)
                        && (accwalkdistance + dist) <= maxdistance) {
                    dist = graphManager.estimateDistance(station.getStation().getPosition(), s.getPosition(), "foot");
                    if ((accwalkdistance + dist) <= maxdistance) {
                        double time = (accwalkdistance + dist) / parameters.expectedWalkingVelocity;
                        IntTuple t = probutilsqueue.getAvailableCapandBikes(s, 0, time);
                        suravcap += t.avcap;
                        suravslots += t.avslots;
                        suravbikes += t.avbikes;
                        surminpostchanges += t.minpostchanges;
                        surmaxpostchanges += t.maxpostchanges;
                        surtakedemandrate += t.takedemandrate;
                        surreturndemandrate += t.returndemandrate;
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            return 0;
        } else {
            int initialbikes = suravbikes;
            initialbikes = Math.max(Math.min(initialbikes, suravcap), 0);
            StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta, UtilitiesProbabilityCalculationQueue.h, surreturndemandrate,
                    surtakedemandrate, suravcap, 1, initialbikes);
            int requiredbikes = 1 + parameters.additionalResourcesDesiredInProbability - surminpostchanges;
            double prob = pc.kOrMoreBikesProbability(requiredbikes);
            return prob;
        }
    }

    private double getSurroundingProbReturn(StationUtilityData station,
            double accbikedistance, List<Station> stations) {
        int suravcap = 0;
        int suravslots = 0;
        int suravbikes = 0;
        int surminpostchanges = 0;
        int surmaxpostchanges = 0;
        double surtakedemandrate = 0;
        double surreturndemandrate = 0;
        boolean found = false;
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            if (s.getId() != station.getStation().getId()) {
                double dist = station.getStation().getPosition().eucleadeanDistanceTo(s.getPosition());
                if (dist <= parameters.MaxDistanceSurroundingStations) {
                    dist = graphManager.estimateDistance(station.getStation().getPosition(), s.getPosition(), "bike");
                    double time = (accbikedistance + dist) / parameters.expectedCyclingVelocity;
                    IntTuple t = probutilsqueue.getAvailableCapandBikes(s, 0, time);
                    suravcap += t.avcap;
                    suravslots += t.avslots;
                    suravbikes += t.avbikes;
                    surminpostchanges += t.minpostchanges;
                    surmaxpostchanges += t.maxpostchanges;
                    surtakedemandrate += t.takedemandrate;
                    surreturndemandrate += t.returndemandrate;
                    found = true;
                }
            }
        }
        if (!found) {
            return 0;
        } else {
            int initialbikes = suravcap - suravslots;
            initialbikes = Math.max(Math.min(initialbikes, suravcap), 0);
            StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta, UtilitiesProbabilityCalculationQueue.h,
                    surreturndemandrate,
                    surtakedemandrate, suravcap, 1, initialbikes);
            int requiredslots = 1 + parameters.additionalResourcesDesiredInProbability + surmaxpostchanges;
            double prob = pc.kOrMoreSlotsProbability(requiredslots);
            return prob;
        }
    }
}
