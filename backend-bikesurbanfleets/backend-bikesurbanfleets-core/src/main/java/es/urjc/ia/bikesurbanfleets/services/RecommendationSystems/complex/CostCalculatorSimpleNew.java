/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ProbabilityData;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
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

    public double calculateCostRentSimple(StationData sd, double sdprob, double time) {
        return calculateCostRent(sdprob, time);
    }

    public double calculateCostReturnSimple(StationData sd, double sdprob, double biketime, double walktime) {
        return calculateCostReturn(sdprob, biketime, walktime);
    }

    public double calculateCostsRentAtStation(StationData sd, List<Station> allstats, double timeintervallforPrediction) {
        //takecosts
        double usercosttake = calculateCostRent(sd.probabilityTake, sd.walktime);

        double timeoffset = sd.walktime;//timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getWalkTime());
        ProbabilityData pd = probutils.calculateFutureProbabilitiesWithAndWithoutArrival(sd.station, timeoffset);
        double takeprobdiff = pd.probabilityTake - pd.probabilityTakeAfterTake;
        double returnprobdiff = pd.probabilityReturnAfterTake - pd.probabilityReturn;

        double extracosttake= takeprobdiff * bestNeighbourRent(sd.station, allstats);
        double gaincostreturn= returnprobdiff * bestNeighbourReturn(sd.station, allstats);
        if (extracosttake<-0.0000000001  || gaincostreturn<-0.0000000001 ){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in take " + sd.station.getId() +  " " + extracosttake+ " " + gaincostreturn );
        }
        //normalize the extracost
        double takerate = probutils.dm.getStationTakeRateIntervall(
                sd.station.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        double returnrate = probutils.dm.getStationReturnRateIntervall(
                sd.station.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        extracosttake = extracosttake * takerate* normmultiplier;
        gaincostreturn = gaincostreturn * returnrate* normmultiplier ;
        
        double globalcost = usercosttake + extracosttake - gaincostreturn;
        sd.individualCost=usercosttake;
        sd.takecostdiff=extracosttake;
        sd.returncostdiff=-gaincostreturn;
        sd.totalCost=globalcost;
        return globalcost;
    }

    public double calculateCostsReturnAtStation(StationData sd, List<Station> allstats, double timeintervallforPrediction) {
        //return costs
        //take a close point to the station as hipotetical detsination
        double timeoffset = sd.biketime;//timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getBiketime());
        double usercostreturn = calculateCostReturn(sd.probabilityReturn, sd.biketime, sd.walktime);

        ProbabilityData pd = probutils.calculateFutureProbabilitiesWithAndWithoutArrival(sd.station, timeoffset);
        double takeprobdiff = pd.probabilityTakeAfterRerturn - pd.probabilityTake;
        double returnprobdiff = pd.probabilityReturn - pd.probabilityReturnAfterReturn;

        double gaincosttake= takeprobdiff * bestNeighbourRent(sd.station, allstats);
        double extracostreturn= returnprobdiff * bestNeighbourReturn(sd.station, allstats);
        if (gaincosttake<-0.0000000001 || extracostreturn<-0.0000000001){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return" + sd.station.getId() +  " " + gaincosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        double takerate = probutils.dm.getStationTakeRateIntervall(
                sd.station.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        double returnrate = probutils.dm.getStationReturnRateIntervall(
                sd.station.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)timeoffset), timeintervallforPrediction);
        gaincosttake = gaincosttake * takerate* normmultiplier;
        extracostreturn = extracostreturn * returnrate* normmultiplier ;

        double globalcost = usercostreturn-gaincosttake+extracostreturn;
        sd.individualCost=usercostreturn;
        sd.takecostdiff=-gaincosttake;
        sd.returncostdiff=extracostreturn;
        sd.totalCost=globalcost;
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
