/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;

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
    public class ExpectedUnsuccessData{
        public double expUnsuccessRentIfNoChange;
        public double expUnsuccessRentAfterTake;
        public double expUnsuccessRentAfterReturn;
        public double expUnsuccessReturnIfNoChange;
        public double expUnsuccessReturnAfterTake;
        public double expUnsuccessReturnAfterReturn;
    }

    // Probabilities form now to timeoffset 
    public abstract double calculateTakeProbability(Station s, double timeoffset) ;
    public abstract double calculateReturnProbability(Station s, double timeoffset) ;
    
    //method for calculation the expected fails (rental and return) if a bike is taken/returned at 
    //currenttime+aarivalofset for users that arrive between 
    //currenttime+aarivalofset and currenttime+aarivalofset+checkintervall
    public abstract ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturn(Station s, double arrivaloffset, double checkintervall);
    public abstract ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRent(Station s, double arrivaloffset, double checkintervall);
            
    public abstract ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturnSurrounding(Station s, double arrivaloffset, double checkintervall,double maxdistancesurrounding, List<Station> allStations);
    public abstract ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRentSurrounding(Station s, double arrivaloffset, double checkintervall, double maxdistancesurrounding, List<Station> allStations);

            
    //methods for calculation probabilities    
    //calculates the probabilities of taking or returning bikes at a station at the moment 
    //currenttime+predictionoffset,
    // if (or if not) a bike is taken/returned at time currenttime+arrivaloffset
    // arrivaltime may be before or after the predictioninterval
    public abstract ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station sd, double arrivaloffset,double predictionoffset) ;
    // this case is the same as the one before, but the arrival is exactly the same as the predictionoffset
    // that is, we predict at currenttime+predictionoffset with and without 1 more bike taken or returnes
    public abstract ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station sd, double predictionoffset) ;
    
   //
    //methods for calculation probabilities    
    //the following values are calculated from currenttime+fromtime up to currenttime+fromtime+duration
    public abstract double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double fromtime,double duration) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double fromtime,double duration) ;
    
    public abstract double calculateExpectedReturns(Station s, double fromtime,double duration) ;
    public abstract double calculateExpectedTakes(Station s, double fromtime,double duration) ;

    //methods for calculation probabilities    
    public abstract double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double fromtime,double duration) ;
    public abstract double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double fromtime,double duration) ;
   
    public  abstract double getGlobalProbabilityImprovementIfTake(StationData sd ) ;

    public abstract double getGlobalProbabilityImprovementIfReturn(StationData sd) ;
}
