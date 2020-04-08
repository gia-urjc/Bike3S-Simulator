/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ProbabilityData;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
public class CostCalculatorSimpleNew {

    //methods for cost calculations
    public CostCalculatorSimpleNew(double maxcost,
            UtilitiesProbabilityCalculator recutils,
            int PredictionNorm, double normmultiplier,
            double walkvel, double cycvel, GraphManager gm) {

        this.probutils = recutils;
        maxCostValue = maxcost;
        predictionNormalisation = PredictionNorm;
        this.normmultiplier = normmultiplier;
        expectedwalkingVelocity = walkvel;
        expectedcyclingVelocity = cycvel;
        this.graphManager = gm;
    }

    final double expectedwalkingVelocity;
    final double expectedcyclingVelocity;
    final GraphManager graphManager;
    final int predictionNormalisation;
    final double maxCostValue;
    UtilitiesProbabilityCalculator probutils;
    final double normmultiplier;
    final double estimatedavwalktimenearest = 150;
    final double estimatedavbiketimenearest = 0;

    private double calculateCostRent(double sdprob, double time) {
        double msdprob = Math.pow(sdprob, 1);
        return time + (1 - msdprob) * maxCostValue;
    }

    private double calculateCostReturn(double sdprob, double biketime, double walktime) {
        double msdprob = Math.pow(sdprob, 1);
        return biketime + msdprob * walktime + (1 - msdprob) * maxCostValue;
    }

    public double calculateCostRentSimple(StationUtilityData sd, double sdprob, double time) {
        return calculateCostRent(sdprob, time);
    }

    public double calculateCostReturnSimple(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        return calculateCostReturn(sdprob, biketime, walktime);
    }

    public double calculateCostsRentAtStation(StationUtilityData sd, List<Station> allstats, double timeintervallforPrediction) {
        //takecosts
        double usercosttake = calculateCostRent(sd.getProbabilityTake(), sd.getWalkTime());

        double timeoffset = sd.getWalkTime();//timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getWalkTime());
        ProbabilityData pd = probutils.calculateFutureTakeProbabilitiesWithArrival(sd.getStation(), 0, timeoffset);
        double takeprobdiff = pd.probabilityTake - pd.probabilityTakeAfterTake;
        double returnprobdiff = pd.probabilityReturnAfterTake - pd.probabilityReturn;

        double extracosttake= takeprobdiff * bestNeighbourRent(sd.getStation(), allstats);
        double gaincostreturn= returnprobdiff * bestNeighbourReturn(sd.getStation(), allstats);
        if (extracosttake<0 || gaincostreturn<0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + gaincostreturn );
        }
        //normalize the extracost
        double takerate = probutils.dm.getStationTakeRateIntervall(
                sd.getStation().getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        double returnrate = probutils.dm.getStationReturnRateIntervall(
                sd.getStation().getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        extracosttake = extracosttake * takerate* normmultiplier;
        gaincostreturn = gaincostreturn * returnrate* normmultiplier ;
        
        double globalcost = usercosttake + extracosttake - gaincostreturn;
        sd.setIndividualCost(usercosttake).setTakecostdiff(extracosttake).setReturncostdiff(-gaincostreturn )
                .setTotalCost(globalcost);
        return globalcost;
    }

    public double calculateCostsReturnAtStation(StationUtilityData sd, List<Station> allstats, double timeintervallforPrediction) {
        //return costs
        //take a close point to the station as hipotetical detsination
        double timeoffset = sd.getBiketime();//timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getBiketime());
        double usercostreturn = calculateCostReturn(sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());

        ProbabilityData pd = probutils.calculateFutureReturnProbabilitiesWithArrival(sd.getStation(), 0, timeoffset);
        double takeprobdiff = pd.probabilityTakeAfterRerturn - pd.probabilityTake;
        double returnprobdiff = pd.probabilityReturn - pd.probabilityReturnAfterReturn;

        double gaincosttake= takeprobdiff * bestNeighbourRent(sd.getStation(), allstats);
        double extracostreturn= returnprobdiff * bestNeighbourReturn(sd.getStation(), allstats);
        if (gaincosttake<0 || extracostreturn<0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + gaincosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        double takerate = probutils.dm.getStationTakeRateIntervall(
                sd.getStation().getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        double returnrate = probutils.dm.getStationReturnRateIntervall(
                sd.getStation().getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        gaincosttake = gaincosttake * takerate* normmultiplier;
        extracostreturn = extracostreturn * returnrate* normmultiplier ;

        double globalcost = usercostreturn-gaincosttake+extracostreturn;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(-gaincosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }

    //with probabilities recalculated at the correct time
    private double bestNeighbourRent(Station s, List<Station> allstats) {
        double newbestValueFound = Double.MAX_VALUE;
        double besttime=0;
        Station bestneighbour = null;
        for (Station nei : allstats) {
            if (!nei.equals(s)) {
                double dist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "foot");
                double newtime = dist / expectedwalkingVelocity;
                double rentprob = probutils.calculateTakeProbability(nei, newtime);
                double thiscost = calculateCostRent(rentprob, newtime);
                if (thiscost < newbestValueFound) {
                    newbestValueFound = thiscost;
                    bestneighbour = nei;
                    besttime=newtime;
                }
            }
        }
        return besttime;
    }

    //with probs recalculated at the correct time
    private double bestNeighbourReturn(Station s, List<Station> allstats) {
        double newbestValueFound = Double.MAX_VALUE;
        double besttime=0;
        Station bestneighbour = null;
        for (Station nei : allstats) {
            if (!nei.equals(s)) {
                double bdist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "bike");
                double btime = bdist / expectedcyclingVelocity;
                double returnprob = probutils.calculateReturnProbability(nei, btime);
                double wdist = graphManager.estimateDistance(nei.getPosition(), s.getPosition(), "foot") / 2D;
                double wtime = wdist / expectedwalkingVelocity;
                double thiscost = calculateCostReturn(returnprob, btime, wtime);
                if (thiscost < newbestValueFound) {
                    newbestValueFound = thiscost;
                    bestneighbour = nei;
                    besttime=btime+wtime;
                }
            }
        }
        return besttime;
    }

}
