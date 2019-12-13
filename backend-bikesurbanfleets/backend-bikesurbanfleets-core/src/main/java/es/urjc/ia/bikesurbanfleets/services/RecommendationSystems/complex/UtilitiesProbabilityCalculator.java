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
    
    DemandManager dm;
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
    public abstract ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant);
    public ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd, double offsetinstantArrivalCurrent, double futureinstant){
        return calculateAllTakeProbabilitiesWithArrival( sd, (long) offsetinstantArrivalCurrent, (long) futureinstant);
    };
    public abstract ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) ;
    public ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, double offsetinstantArrivalCurrent, double futureinstant){
        return calculateAllReturnProbabilitiesWithArrival( sd, (long) offsetinstantArrivalCurrent, (long) futureinstant);
    };
    public abstract ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) ;
    public ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, double offsetinstantArrivalCurrent, double futureinstant){
        return calculateAllProbabilitiesWithArrival( sd, (long) offsetinstantArrivalCurrent, (long) futureinstant);
    };
    
    //methods for calculation probabilities    
    public abstract double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double timeoffset) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double timeoffset) ;
    
    public abstract double calculateExpectedReturns(Station s, double timeoffset) ;
    public abstract double calculateExpectedTakes(Station s, double timeoffset) ;

    //methods for calculation probabilities    
    public abstract double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double timeoffset) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double timeoffset) ;
   
    public  abstract double getGlobalProbabilityImprovementIfTake(StationUtilityData sd ) ;

    public abstract double getGlobalProbabilityImprovementIfReturn(StationUtilityData sd) ;
}
