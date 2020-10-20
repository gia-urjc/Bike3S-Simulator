/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

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
public class CostCalculatorSimple {

    //methods for cost calculations
    public CostCalculatorSimple(double maxcost,
            UtilitiesProbabilityCalculator recutils,
            int PredictionNorm, double normmultiplier,
            double walkvel, double cycvel, GraphManager gm) {
        this.probutils=recutils;
        maxCostValue=maxcost;
        predictionNormalisation=PredictionNorm;
        this.normmultiplier=normmultiplier;
        graphManager=gm;
        expectedwalkingVelocity=walkvel;
        expectedcyclingVelocity=cycvel;
    }

    final double expectedwalkingVelocity;
    final double expectedcyclingVelocity;    
    final GraphManager graphManager;
    final int predictionNormalisation;
    final double maxCostValue;
    UtilitiesProbabilityCalculator probutils;
    final double normmultiplier;
    final double estimatedavwalktimenearest=150;
    final double estimatedavbiketimenearest=0;

    private double calculateCostRent(StationUtilityData sd, double sdprob, double time) {
        double msdprob = Math.pow(sdprob,1);
     //   double msdprob = 1-(1D/(1D+Math.pow(2,(10D*(sdprob-0.75D)))));
   //     return (-Math.log10((sdprob)))* maxCostValue +time;
   //         if (time >maxCostValue) return time;
  //          return  sdprob* time + (1-sdprob)* maxCostValue;
            return  time + (1-msdprob)* maxCostValue;
            
    }

    private double calculateCostReturn(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        double msdprob = Math.pow(sdprob,1);
    //    double msdprob = 1-(1D/(1D+Math.pow(2,(10D*(sdprob-0.75D)))));
        
        double time= biketime+ walktime;
  //      return (-Math.log10((sdprob)))* maxCostValue +time;
   //         if (time >maxCostValue) return time;
 //           return  sdprob* time+ (1-sdprob)* maxCostValue;
            return  biketime+ msdprob*walktime+ (1-msdprob)* maxCostValue;
    }

    public double calculateCostRentSimple(StationUtilityData sd, double sdprob, double time) {
        return calculateCostRent(sd,sdprob,time);
    }

    public double calculateCostReturnSimple(StationUtilityData sd, double sdprob, double biketime, double walktime) {
        return calculateCostReturn(sd,sdprob,biketime,walktime);
    }

    public double calculateCostsRentAtStation(StationUtilityData sd, List<Station> allstats,double timeintervallforPrediction) {
        //takecosts
        double usercosttake = calculateCostRent(sd, sd.getProbabilityTake(), sd.getWalkTime());
        
        double timeoffset=timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getWalkTime());
        ProbabilityData pd=probutils.calculateFutureProbabilitiesWithAndWithoutArrival(sd.getStation(),timeoffset);

       
        //analyze global costs
        //takecost if bike is taken   
        double costtake = calculateCostRent(sd, pd.probabilityTake, estimatedavwalktimenearest);
        double costtakeafter = calculateCostRent(sd, pd.probabilityTakeAfterTake,estimatedavwalktimenearest);
        //return costs
        //take a close point to the station as hipotetical detsination
        double costreturn = calculateCostReturn(sd, pd.probabilityReturn, estimatedavbiketimenearest, estimatedavwalktimenearest);
        double costreturnafter = calculateCostReturn(sd, pd.probabilityReturnAfterTake, estimatedavbiketimenearest, estimatedavwalktimenearest);

        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafter - costreturn;
        if (extracostreturn>0.0000000001 || extracosttake<-0.0000000001){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), 0,timeoffset) * sd.getProbabilityTake();
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), 0,timeoffset) * sd.getProbabilityTake();
        
  //      extracosttake = Math.min(extracosttake, 600);
   //     extracostreturn = Math.max(extracostreturn, 0);

        double globalcost = usercosttake + extracosttake + extracostreturn;
        sd.setIndividualCost(usercosttake).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
    public double calculateCostsReturnAtStation(StationUtilityData sd, List<Station> allstats,double timeintervallforPrediction) {
        //return costs
        //take a close point to the station as hipotetical detsination
        double timeoffset=timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getBiketime());
        double usercostreturn = calculateCostReturn(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());

        ProbabilityData pd=probutils.calculateFutureProbabilitiesWithAndWithoutArrival(sd.getStation(), timeoffset);
        //analyze global costs
        //takecost if bike is taken   
        double costtake = calculateCostRent(sd, pd.probabilityTake, estimatedavwalktimenearest);
        double costtakeafter = calculateCostRent(sd, pd.probabilityTakeAfterRerturn, estimatedavwalktimenearest);

        //return costs
        //take a close point to the station as hipotetical detsination
        double costreturnhip = calculateCostReturn(sd, pd.probabilityReturn, estimatedavbiketimenearest, estimatedavwalktimenearest);
        double costreturnafterhip = calculateCostReturn(sd, pd.probabilityReturnAfterReturn,estimatedavbiketimenearest, estimatedavwalktimenearest);
        double extracosttake = costtakeafter - costtake;
        double extracostreturn = costreturnafterhip - costreturnhip;
        if (extracostreturn<-0.0000000001 || extracosttake>0.0000000001){
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
        }
        //normalize the extracost
        extracosttake = extracosttake * getTakeFactor(sd.getStation(), 0,timeoffset) * sd.getProbabilityReturn();
        extracostreturn = extracostreturn* getReturnFactor(sd.getStation(), 0,timeoffset) * sd.getProbabilityReturn();
  //      extracosttake = Math.max(extracosttake, -300);
   //     extracostreturn = Math.min(extracostreturn, 0);

        double globalcost = usercostreturn+extracosttake+extracostreturn;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(extracosttake).setReturncostdiff(extracostreturn)
                .setTotalCost(globalcost);
        return globalcost;
    }
     
    private double getTakeFactor(Station s, double expectedarrivaltime,double timeintervallforPrediction){
        double fixedmult=normmultiplier;
        double returnrate=probutils.dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)expectedarrivaltime), 
                timeintervallforPrediction);
        double takerate=probutils.dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)expectedarrivaltime), 
                timeintervallforPrediction);
        double probtake=probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,expectedarrivaltime,timeintervallforPrediction);
        double diff=Math.max(0,takerate-returnrate);
        double takeonlyprob=probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s,expectedarrivaltime,timeintervallforPrediction);;
        double takeexpected=probutils.calculateExpectedTakes(s, expectedarrivaltime,timeintervallforPrediction);

      /*  System.out.println("take Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "takerate " + takerate + " " + 
               "probtake " + probtake + " " + 
               "diff " + diff + " " + 
                "takeonlyprob " + takeonlyprob + " "  +
                 "takeexpected " + takeexpected + " "  +
              "retu rate " +returnrate+ " "  +
                "take rate " +takerate+ " " 
               );
      */  switch(predictionNormalisation){
            case (0) :
                return normmultiplier;
            case (1) :
                return normmultiplier*probtake;
            case (2) :
                return normmultiplier*diff;
            case (3) :
                return normmultiplier*(takerate+returnrate)/2;
            case (4) :
                return normmultiplier*takeexpected;
            case (5) :
                return normmultiplier*((takerate+returnrate)/2+probtake);
            case (6) :
                return normmultiplier* takerate;
            case (7) :
                return normmultiplier*takeonlyprob;
            case (8) :
                return normmultiplier*takeonlyprob*probtake;
        }
         return 1;
    }
     private double getReturnFactor(Station s, double expectedarrivaltime,double timeintervallforPrediction){
        double fixedmult=normmultiplier;
        double returnrate=probutils.dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)expectedarrivaltime), 
                timeintervallforPrediction);
        double takerate=probutils.dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)expectedarrivaltime), 
                timeintervallforPrediction);
        double probreturn=probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,expectedarrivaltime,timeintervallforPrediction);
        double diff=Math.max(0,returnrate-takerate);
        double returnonlyprob=probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,expectedarrivaltime,timeintervallforPrediction);
        double returnexpected=probutils.calculateExpectedReturns(s, expectedarrivaltime,timeintervallforPrediction);
 
    /*    System.out.println("retu Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "returate " + returnrate + " " + 
               "probretu " + probreturn + " " + 
               "diff " + diff + " " + 
                "retuonlyprob " + returnonlyprob + " "  +
                "retuexpected " + returnexpected + " "  +
                "retu rate " +returnrate+ " "  +
                "take rate " +takerate+ " " 
                
               );
     */   switch(predictionNormalisation){
            case (0) :
                 return normmultiplier;
            case (1) :
                return normmultiplier*probreturn;
            case (2) :
                return normmultiplier*diff;
            case (3) :
                return normmultiplier*(returnrate+takerate)/2;
            case (4) :
                return normmultiplier*returnexpected;
            case (5) :
                return normmultiplier*( (returnrate+takerate)/2+probreturn );
            case (6) :
                return normmultiplier*returnrate;
           case (7) :
                return normmultiplier*returnonlyprob;
           case (8) :
                return normmultiplier*returnonlyprob*probreturn;
        }
         return 1;
    }
 
}
