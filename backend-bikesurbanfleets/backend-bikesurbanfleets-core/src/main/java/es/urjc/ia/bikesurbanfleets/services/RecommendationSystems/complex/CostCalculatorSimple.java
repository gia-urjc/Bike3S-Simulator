/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ProbabilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
public class CostCalculatorSimple {

    //methods for cost calculations
    public CostCalculatorSimple(double maxcost,
            double walkvel, double cycvel, 
            UtilitiesProbabilityCalculator recutils,
            int PredictionNorm, double normmultiplier) {
        walkingVelocity=walkvel;
        cyclingVelocity=cycvel;
        this.probutils=recutils;
        maxCostValue=maxcost;
        predictionNormalisation=PredictionNorm;
        this.normmultiplier=normmultiplier;
    }

    final int predictionNormalisation;
    final double maxCostValue;
    final double walkingVelocity;
    final double cyclingVelocity;
    UtilitiesProbabilityCalculator probutils;
    final double normmultiplier;
    final double estimatedavwalktimenearest=150;
    final double estimatedavbiketimenearest=1000;


    public double calculateCostRentSimple(StationUtilityData sd, double sdprob, double time) {
            if (time >maxCostValue) return time;
            return  sdprob * time + (1-sdprob)* maxCostValue;
    }

    public double calculateCostReturnSimple(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        double time= biketime+ walktime;
            if (time >maxCostValue) return time;
            return  sdprob * time+ (1-sdprob)* maxCostValue;
    }

    public double calculateCostsRentAtStation(StationUtilityData sd, double timeintervallforPrediction) {
        //takecosts
        double usercosttake = calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());
        
        double timeoffset=Math.max(timeintervallforPrediction, sd.getWalkTime());
        ProbabilityData pd=probutils.calculateAllTakeProbabilitiesWithArrival(sd, sd.getWalkTime(),timeoffset);

       
        //analyze global costs
        //takecost if bike is taken   
        double costtake = calculateCostRentSimple(sd, pd.probabilityTake, estimatedavwalktimenearest);
        double costtakeafter = calculateCostRentSimple(sd, pd.probabilityTakeAfterTake,estimatedavwalktimenearest);
        //return costs
        //take a close point to the station as hipotetical detsination
        double costreturn = calculateCostReturnSimple(sd, pd.probabilityReturn, estimatedavbiketimenearest, estimatedavwalktimenearest);
        double costreturnafter = calculateCostReturnSimple(sd, pd.probabilityReturnAfterTake, estimatedavbiketimenearest, estimatedavwalktimenearest);

        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafter - costreturn;
        if (extracostreturn>0 || extracosttake<0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), timeoffset) * sd.getProbabilityTake();
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), timeoffset) * sd.getProbabilityTake();
        
        double globalcost = usercosttake + extracosttake + 0*extracostreturn;
        sd.setIndividualCost(usercosttake).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
    public double calculateCostsReturnAtStation(StationUtilityData sd, double timeintervallforPrediction) {
        //return costs
        //take a close point to the station as hipotetical detsination
        double timeoffset=Math.max(timeintervallforPrediction, sd.getBiketime());
        double usercostreturn = calculateCostReturnSimple(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());

        ProbabilityData pd=probutils.calculateAllReturnProbabilitiesWithArrival(sd, sd.getBiketime(), timeoffset);
        //analyze global costs
        //takecost if bike is taken   
        double costtake = calculateCostRentSimple(sd, pd.probabilityTake, estimatedavwalktimenearest);
        double costtakeafter = calculateCostRentSimple(sd, pd.probabilityTakeAfterRerturn, estimatedavwalktimenearest);

        //return costs
        //take a close point to the station as hipotetical detsination
        double costreturnhip = calculateCostReturnSimple(sd, pd.probabilityReturn, estimatedavbiketimenearest, estimatedavwalktimenearest);
        double costreturnafterhip = calculateCostReturnSimple(sd, pd.probabilityReturnAfterReturn,estimatedavbiketimenearest, estimatedavwalktimenearest);
        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafterhip - costreturnhip;
        if (extracostreturn<0 || extracosttake>0){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), timeoffset) * sd.getProbabilityReturn();
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), timeoffset)* sd.getProbabilityReturn();

        double globalcost = usercostreturn+extracosttake+0.5*extracostreturn;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
    
    private double getTakeFactor(Station s, double timeoffset){
        double fixedmult=normmultiplier;
        double takerate=probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double probtake=probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset);
        double diff=Math.max(0,probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        double takeonlyprob=probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s,timeoffset);;
        double takeexpected=probutils.calculateExpectedTakes(s, timeoffset);

   /*     System.out.println("take Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "takerate " + takerate + " " + 
               "probtake " + probtake + " " + 
               "diff " + diff + " " + 
                "takeonlyprob " + takeonlyprob + " "  +
                 "takeexpected " + takeexpected + " "  +
              "retu rate " +probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " "  +
                "take rate " +probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " " 
               );
    */    switch(predictionNormalisation){
            case (0) :
                return normmultiplier;
            case (1) :
                return normmultiplier*probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset);
            case (2) :
                return normmultiplier*Math.max(0,probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (3) :
                return normmultiplier*(probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset))/2;
            case (4) :
                return normmultiplier*probutils.calculateExpectedTakes(s, timeoffset);
            case (5) :
                return normmultiplier*(
                        ((probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset))/2)+
                        (probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset)));
            case (6) :
                return normmultiplier*probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (7) :
                return normmultiplier*probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s,timeoffset);
        }
         return 1;
    }
     private double getReturnFactor(Station s, double timeoffset){
        double fixedmult=normmultiplier;
        double returnrate=probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double probreturn=probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset);
        double diff=Math.max(0,probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        double returnonlyprob=probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,timeoffset);
        double returnexpected=probutils.calculateExpectedReturns(s, timeoffset);
 
 /*       System.out.println("retu Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "returate " + returnrate + " " + 
               "probretu " + probreturn + " " + 
               "diff " + diff + " " + 
                "retuonlyprob " + returnonlyprob + " "  +
                "retuexpected " + returnexpected + " "  +
                "retu rate " +probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " "  +
                "take rate " +probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " " 
                
               );
   */     switch(predictionNormalisation){
            case (0) :
                 return normmultiplier;
            case (1) :
                return normmultiplier*probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset);
            case (2) :
                return normmultiplier*Math.max(0,probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (3) :
                return normmultiplier*(probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset))/2;
            case (4) :
                return normmultiplier*probutils.calculateExpectedReturns(s, timeoffset);
            case (5) :
                return normmultiplier*(
                        ((probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset))/2)+
                        (probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset)));
            case (6) :
                return normmultiplier*probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
           case (7) :
                return normmultiplier*probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,timeoffset);
        }
         return 1;
    }
 
}
