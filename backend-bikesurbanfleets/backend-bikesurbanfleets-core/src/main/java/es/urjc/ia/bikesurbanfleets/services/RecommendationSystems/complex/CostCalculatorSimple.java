/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;

/**
 *
 * @author holger
 */
public class CostCalculatorSimple {

    //methods for cost calculations
    public CostCalculatorSimple(
            double unsucesscostRentPenalisation,
            double unsucesscostReturnPenalisation) {
        unsucesscostRent=unsucesscostRentPenalisation;
        unsucesscostReturn=unsucesscostReturnPenalisation;
    }

    final double unsucesscostRent;
    final double unsucesscostReturn;


    public double calculateCostRentSimple(StationData sd, double sdprob, double time) {
     //   double msdprob = 1-(1D/(1D+Math.pow(2,(10D*(sdprob-0.75D)))));
   //     return (-Math.log10((sdprob)))* maxCostValue +time;
   //         if (time >maxCostValue) return time;
  //          return  sdprob* time + (1-sdprob)* maxCostValue;
            return  time + (1-sdprob)* unsucesscostRent; //buena opción 2
  //          return  time + (1-sdprob)* 30000; //buena opción 2
 //           if (msdprob<0.0000001) return 100000;
 //           return  Math.pow(time, 0.01D)/ (sdprob); //buena combinación1
}

    public double calculateCostReturnSimple(StationData sd, double sdprob, double biketime, double walktime) {
    //    double msdprob = 1-(1D/(1D+Math.pow(2,(10D*(sdprob-0.75D)))));
        
        double time= biketime+ walktime;
  //      return (-Math.log10((sdprob)))* maxCostValue +time;
   //         if (time >maxCostValue) return time;
 //           return  sdprob* time+ (1-sdprob)* maxCostValue;
 //           return  time+  (1-msdprob)* maxCostValue;
 //           return  biketime+ sdprob*walktime+ (1-sdprob)* 3000;//buena opción 2
            if (walktime>unsucesscostReturn) return biketime+walktime; 
            return  biketime+ sdprob*walktime+ (1-sdprob)* unsucesscostReturn;//buena opción 2
//            if (msdprob<0.0000001) return 100000;
 //         return  Math.pow(time, 0.4D)/ (sdprob); //buena combinación1
    } 
}
