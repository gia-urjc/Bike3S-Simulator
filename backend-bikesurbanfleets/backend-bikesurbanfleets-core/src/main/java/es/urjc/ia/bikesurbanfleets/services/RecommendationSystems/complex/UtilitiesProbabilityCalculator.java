/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

/**
 *
 * @author holger
 */
public abstract class UtilitiesProbabilityCalculator {
    
    protected DemandManager dm;
    public class ProbabilityData{
        public double probabilityTake;
        public double probabilityReturn;
        public double probabilityTakeAfterTake;
        public double probabilityReturnAfterTake;
        public double probabilityTakeAfterRerturn;
        public double probabilityReturnAfterReturn;
    }

    // Probabilities form now to timeoffset 
    public abstract double calculateTakeProbability(Station s, double timeoffset) ;
    public abstract double calculateReturnProbability(Station s, double timeoffset) ;
    
    //methods for calculation probabilities    
    public abstract ProbabilityData calculateFutureTakeProbabilitiesWithArrival(Station sd, double fromtime,double totime);
    public abstract ProbabilityData calculateFutureReturnProbabilitiesWithArrival(Station sd, double fromtime,double totime) ;
    public abstract ProbabilityData calculateFutureAllProbabilitiesWithArrival(Station sd, double fromtime,double totime) ;
    
    //methods for calculation probabilities    
    public abstract double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double fromtime,double totime) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double fromtime,double totime) ;
    
    public abstract double calculateExpectedReturns(Station s, double fromtime,double totime) ;
    public abstract double calculateExpectedTakes(Station s, double fromtime,double totime) ;

    //methods for calculation probabilities    
    public abstract double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double fromtime,double totime) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double fromtime,double totime) ;
   
    public  abstract double getGlobalProbabilityImprovementIfTake(StationUtilityData sd ) ;

    public abstract double getGlobalProbabilityImprovementIfReturn(StationUtilityData sd) ;
}
