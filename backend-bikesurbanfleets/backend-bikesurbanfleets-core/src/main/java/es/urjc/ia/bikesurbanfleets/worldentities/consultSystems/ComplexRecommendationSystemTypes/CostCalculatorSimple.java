/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
public class CostCalculatorSimple {

    //methods for cost calculations
    public CostCalculatorSimple(double marginprob, double maxcost,
            double walkvel, double cycvel, 
            double maxDistanceRecomendation, UtilitiesForRecommendationSystems recutils,
            boolean squaredTimes, int PredictionNorm) {
        minimumMarginProbability = marginprob;
        walkingVelocity=walkvel;
        cyclingVelocity=cycvel;
        this.maxDistanceRecomendation=maxDistanceRecomendation;
        maxWalktime=this.maxDistanceRecomendation/walkingVelocity;
        this.recutils=recutils;
        maxCostValue=maxcost;
        useSuaredTimes=squaredTimes;
        predictionNormalisation=PredictionNorm;
    }

    final boolean useSuaredTimes;
    final int predictionNormalisation;
    final double minimumMarginProbability;
    final double maxCostValue;
    final double walkingVelocity;
    final double cyclingVelocity;
    final double maxDistanceRecomendation;
    UtilitiesForRecommendationSystems recutils;
    final double maxWalktime;


    private double getSqarewalkTimeRent(double accwalktime) {
        if (useSuaredTimes)
        return (accwalktime*accwalktime)/maxWalktime;
        else return accwalktime;
    }
    private double getSqareReturnDistanceCost(double accbiketime, double walktime) {
        if (useSuaredTimes)
            return (accbiketime + walktime*walktime)/maxWalktime;
        else return accbiketime+walktime;
    }

    public double calculateCostRentSimple(StationUtilityData sd, double sdprob, double time) {
        double sqtime=getSqarewalkTimeRent(time);
        if(sdprob>1-minimumMarginProbability){
            return (1 - minimumMarginProbability) * sqtime;
        } else {
            return (sdprob*sqtime)+ (1-minimumMarginProbability-sdprob)* getSqarewalkTimeRent(maxCostValue);
        }
    }

    public double calculateCostReturnSimple(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        double sqtime=getSqareReturnDistanceCost(biketime, walktime);
        if(sdprob>1-minimumMarginProbability){
            return (1 - minimumMarginProbability) * (sqtime);
        } else {
            return (sdprob * (sqtime))+ (1-minimumMarginProbability-sdprob)* getSqareReturnDistanceCost(biketime,maxCostValue);
        }
    }

    public double calculateCostsRentAtStation(StationUtilityData sd) {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        double usercosttake = calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());
        int timeoffset = (int)sd.getWalkTime();

        //analyze global costs
        //takecost if bike is taken     
        lookedlist.clear();
        double costtake = calculateCostRentSimple(sd, sd.getProbabilityTake(), 0);
        lookedlist.clear();
        double costtakeafter = calculateCostRentSimple(sd, sd.getProbabilityTakeAfterTake(),0);
        //return costs
        //take a close point to the station as hipotetical detsination
        lookedlist.clear();
        double costreturn = calculateCostReturnSimple(sd, sd.getProbabilityReturn(), 0, 0);
        lookedlist.clear();
        double costreturnafter = calculateCostReturnSimple(sd, sd.getProbabilityReturnAfterTake(), 0, 0);

        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafter - costreturn;
        if (extracostreturn>0 || extracosttake<0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), timeoffset);
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), timeoffset);;
        
        double globalcost = usercosttake + extracosttake + extracostreturn;
        sd.setIndividualCost(usercosttake).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
    public double calculateCostsReturnAtStation(StationUtilityData sd) {
        //return costs
        //take a close point to the station as hipotetical detsination
        int timeoffset = (int) (sd.getBiketime());
        List<StationUtilityData> lookedlist = new ArrayList<>();
        double usercostreturn = calculateCostReturnSimple(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());

        //analyze global costs
        //takecost if bike is taken   
        lookedlist.clear();
        double costtake = calculateCostRentSimple(sd, sd.getProbabilityTake(), 0);
        lookedlist.clear();
        double costtakeafter = calculateCostRentSimple(sd, sd.getProbabilityTakeAfterRerturn(), 0);

        //return costs
        //take a close point to the station as hipotetical detsination
        lookedlist.clear();
        double costreturnhip = calculateCostReturnSimple(sd, sd.getProbabilityReturn(), 0, 0);
        lookedlist.clear();
        double costreturnafterhip = calculateCostReturnSimple(sd, sd.getProbabilityReturnAfterReturn(),0, 0);
        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafterhip - costreturnhip;
        if (extracostreturn<0 || extracosttake>0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), timeoffset);
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), timeoffset);;

        double globalcost = usercostreturn+extracosttake+extracostreturn;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
    
    private double getTakeFactor(Station s, double timeoffset){
         switch(predictionNormalisation){
            case (0) :
                return 1;
            case (1) :
                return recutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return recutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset);
            case (3) :
                return recutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s,timeoffset);
        }
         return 1;
    }
     private double getReturnFactor(Station s, double timeoffset){
        switch(predictionNormalisation){
            case (0) :
                 return 1;
            case (1) :
                return recutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return recutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset);
            case (3) :
                return recutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,timeoffset);
        }
         return 1;
    }
 
}
