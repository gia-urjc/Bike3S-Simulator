/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.StationProbabilitiesQueueBased;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import static es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.AbstractRecommendationSystemDemandProbabilityBased.costRentComparator;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculationQueue.IntTuple;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DEMAND_cost_surrounding")
public class RecommendationSystemDemandProbabilityCostSurrounding extends AbstractRecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends AbstractRecommendationSystemDemandProbabilityBased.RecommendationParameters {

        private double desireableProbability = 0.8;
        private double unsucesscostRentPenalisation = 3000; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturnPenalisation = 30000; //with calculator2bis=between 4000 and 6000
        private double MaxDistanceSurroundingStations = 500;
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
                parameters.unsucesscostRentPenalisation,
                parameters.unsucesscostReturnPenalisation,
                probutils, 0, 0,
                parameters.expectedWalkingVelocity,
                parameters.expectedCyclingVelocity,
                graphManager);
        probutilsqueue = (UtilitiesProbabilityCalculationQueue) probutils;
    }

    @Override
    protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        return stationdata
                .map(sd -> {
                    double prob = sd.probabilityTake;

                    double secprob = getSurroundingProbRent(sd, sd.walkdist, allstations, maxdistance);
                    double secextracost = (maxdistance - sd.walktime) / 2D;
                    double cost = sd.walktime + (1 - prob)
                            * (secextracost + (1 - secprob) * parameters.unsucesscostRentPenalisation);

                    sd.totalCost = cost;
                    return sd;
                })//apply function to calculate cost 
                .sorted(costRentComparator(parameters.desireableProbability));
    }

    @Override
    protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        return stationdata
                .map(sd -> {
                    double prob = sd.probabilityReturn;

                    double secprob = getSurroundingProbReturn(sd, sd.bikedist, allstations);
                    double secextracostbike = parameters.MaxDistanceSurroundingStations  / 2D;;

                    double cost = sd.biketime + sd.walktime + (1 - prob)
                            * (secextracostbike + (1 - secprob) * parameters.unsucesscostReturnPenalisation);
                    sd.totalCost = cost;
                    return sd;
                })//apply function to calculate cost
                .sorted(costReturnComparator(parameters.desireableProbability));
    }

    private double getSurroundingProbRent(StationData station,
            double accwalkdistance, List<Station> stations, double maxdistance) {
        int suravcap = 0;
        int suravslots = 0;
        int suravbikes = 0;
        int surminpostchanges = 0;
        int surmaxpostchanges = 0;
        double surtakedemandrate = 0;
        double surreturndemandrate = 0;
        boolean found = false;
        List<StationData> temp = new ArrayList<>();
        for (Station s : stations) {
            if (s.getId() != station.station.getId()) {
                double dist = station.station.getPosition().eucleadeanDistanceTo(s.getPosition());
                if ((dist <= parameters.MaxDistanceSurroundingStations)
                        && (accwalkdistance + dist) <= maxdistance) {
                    dist = graphManager.estimateDistance(station.station.getPosition(), s.getPosition(), "foot");
                    if ((accwalkdistance + dist) <= maxdistance) {
                        double time = (accwalkdistance + dist) / parameters.expectedWalkingVelocity;
                        IntTuple t = probutilsqueue.getAvailableCapandBikes(s, time);
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
                    surtakedemandrate, suravcap, initialbikes);
            int requiredbikes = 1 + parameters.additionalResourcesDesiredInProbability - surminpostchanges;
            double prob = pc.kOrMoreBikesProbability(requiredbikes);
            return prob;
        }
    }

    private double getSurroundingProbReturn(StationData station,
            double accbikedistance, List<Station> stations) {
        int suravcap = 0;
        int suravslots = 0;
        int suravbikes = 0;
        int surminpostchanges = 0;
        int surmaxpostchanges = 0;
        double surtakedemandrate = 0;
        double surreturndemandrate = 0;
        boolean found = false;
        List<StationData> temp = new ArrayList<>();
        for (Station s : stations) {
            if (s.getId() != station.station.getId()) {
                double dist = station.station.getPosition().eucleadeanDistanceTo(s.getPosition());
                if (dist <= parameters.MaxDistanceSurroundingStations) {
                    dist = graphManager.estimateDistance(station.station.getPosition(), s.getPosition(), "bike");
                    double time = (accbikedistance + dist) / parameters.expectedCyclingVelocity;
                    IntTuple t = probutilsqueue.getAvailableCapandBikes(s, time);
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
                    surtakedemandrate, suravcap, initialbikes);
            int requiredslots = 1 + parameters.additionalResourcesDesiredInProbability + surmaxpostchanges;
            double prob = pc.kOrMoreSlotsProbability(requiredslots);
            return prob;
        }
    }
}
