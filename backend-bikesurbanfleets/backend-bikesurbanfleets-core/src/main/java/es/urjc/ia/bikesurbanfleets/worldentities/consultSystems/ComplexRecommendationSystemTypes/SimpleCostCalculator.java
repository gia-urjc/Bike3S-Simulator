/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
public class SimpleCostCalculator {

    //methods for cost calculations
    public SimpleCostCalculator(double marginprob, double maxcostrent, double maxcostreturn) {
        minimumMarginProbability = marginprob;
        maxCostValueRent = maxcostrent;
        maxCostValueReturn = maxcostreturn;
    }

    final double minimumMarginProbability;
    final double maxCostValueRent;
    final double maxCostValueReturn;

    private double calculateCostRentSimple(StationUtilityData sd, double sdprob, double time) {
        if(sdprob>1-minimumMarginProbability){
            return (1 - minimumMarginProbability) * time;
        } else {
            return (sdprob*time)+ (1-minimumMarginProbability-sdprob)* maxCostValueRent;
        }
    }

    private double calculateCostReturnSimple(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        double thisbikecost = biketime;
        double thiswalkcost = walktime;
        if(sdprob>1-minimumMarginProbability){
            return (1 - minimumMarginProbability) * (thisbikecost+thiswalkcost);
        } else {
            return (sdprob * (thiswalkcost+thiswalkcost))+ (1-minimumMarginProbability-sdprob)* maxCostValueReturn;
        }
    }

    public double calculateCostsRentAtStation(StationUtilityData sd,
            double demandfactor, UtilitiesForRecommendationSystems urs) {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        double usercosttake = calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());

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

        double difftakecost = costtakeafter - costtake;
        double diffretcost = costreturnafter - costreturn;
        //normalize costdiferences to demand
        int timeoffset = (int) sd.getWalkTime();
        double futtakedemand = urs.getFutureBikeDemand(sd.getStation(), timeoffset);
        double futreturndemand = urs.getFutureSlotDemand(sd.getStation(), timeoffset);
        double futglobaltakedem = urs.getFutureGlobalBikeDemand(timeoffset);
        double futglobalretdem = urs.getFutureGlobalSlotDemand(timeoffset);
        difftakecost = difftakecost * futtakedemand * demandfactor;
        diffretcost = diffretcost * futreturndemand * demandfactor;

        double globalcost = usercosttake + difftakecost + diffretcost;
        sd.setIndividualCost(usercosttake).setTakecostdiff(difftakecost).setReturncostdiff(diffretcost)
                .setTotalCost(globalcost);
        return globalcost;
    }
    public double calculateCostsReturnAtStation(StationUtilityData sd,
            double demandfactor, UtilitiesForRecommendationSystems urs) {
        //return costs
        //take a close point to the station as hipotetical detsination
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
        double difftakecost = costtakeafter - costtake;
        double diffretcost = costreturnafterhip - costreturnhip;
        //noirmalize to demand
        int timeoffset = (int) sd.getBiketime();
        double futtakedemand = urs.getFutureBikeDemand(sd.getStation(), timeoffset);
        double futreturndemand = urs.getFutureSlotDemand(sd.getStation(), timeoffset);
        double futglobaltakedem = urs.getFutureGlobalBikeDemand(timeoffset);
        double futglobalretdem = urs.getFutureGlobalSlotDemand(timeoffset);
        difftakecost = difftakecost * futtakedemand * demandfactor;
        diffretcost = diffretcost * futreturndemand * demandfactor;

        double globalcost = usercostreturn+difftakecost+diffretcost;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(difftakecost).setReturncostdiff(diffretcost)
                .setTotalCost(globalcost);
        return globalcost;
    }
}
