/*
Here probability is calculatest as follows:
P(finding a bike in sercain time)=P(x>=k) where k=1-currentbikes-expected bikes in future
The probability is calculated through skellam
That is here, expected bikes in the futer (or takes of bikes) are treated as if they have already happened

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import es.urjc.ia.bikesurbanfleets.common.util.ProbabilityDistributions;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;

/**
 *
 * @author holger
 */
public class UtilitiesProbabilityCalculationSkellam extends UtilitiesProbabilityCalculator{
  
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    final private double probabilityExponent;
    
    public UtilitiesProbabilityCalculationSkellam(double probabilityExponent,DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
            boolean takeintoaccountexpected, boolean takeintoaccountcompromised, int additionalResourcesDesiredInProbability
    ) {
        this.dm = dm;
        this.probabilityExponent=probabilityExponent;
        this.pastrecs=pastrecs;
        this.probabilityUsersObey=probabilityUsersObey;
        this.takeintoaccountexpected=takeintoaccountexpected;
        this.takeintoaccountcompromised=takeintoaccountcompromised;
        if (additionalResourcesDesiredInProbability<0 || additionalResourcesDesiredInProbability>3){
            throw new RuntimeException("invalid parameters");
        }
        this.additionalResourcesDesiredInProbability=additionalResourcesDesiredInProbability;
    }

        private class IntTuple{
        int avcap;
        int avslots;
        int avbikes;
        int changes;
        int minpostchanges=0;
        int maxpostchanges=0;
        double takedemandrate;
        double returndemandrate;
    }
    // get current capacity and available bikes
    // this takes away reserved bikes and slots and takes into account expected changes
    private IntTuple getAvailableCapandBikes(Station s, double timeoffset) {
        IntTuple res =new IntTuple();
        res.avcap=s.getCapacity()-s.getReservedBikes()-s.getReservedSlots();
        res.avbikes=s.availableBikes();
        res.avslots=res.avcap-res.avbikes;
        if (takeintoaccountexpected) {
             PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), 0, timeoffset);
            res.avbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            res.avslots =res.avslots- (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                res.maxpostchanges=(int) er.maxpostchanges ;
                res.minpostchanges=(int) er.minpostchanges ;
            }
        }
        if (res.avbikes<0) res.avbikes=0;
        if (res.avbikes>res.avcap) res.avbikes=res.avcap;
        if (res.avslots<0) res.avslots=0;
        if (res.avslots>res.avcap) res.avslots=res.avcap;
        res.takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);        
        return res;
    }

   // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        //if there are other demands already registered, dont' take the last bike
   //     if (avCB.avbikes <=-avCB.minpostchanges && avCB.minpostchanges<0) {
   //         return 0;
   //     }
        int estimatedbikes = avCB.avbikes + avCB.minpostchanges- additionalResourcesDesiredInProbability;
        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double prob = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.returndemandrate, avCB.takedemandrate, k);
        return Math.pow(prob, probabilityExponent); 
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        //if there are other demands already registered, dont' take the last bike
    //    if (avCB.avslots <=avCB.maxpostchanges && avCB.maxpostchanges>0) {
     //       return 0;
     //   }
        int estimatedslots=(avCB.avslots - avCB.maxpostchanges)- additionalResourcesDesiredInProbability;
        //probability that a slot exists and that is exists after taking one 
        int k = 1 - estimatedslots;
        double prob = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.takedemandrate, avCB.returndemandrate, k);
        return Math.pow(prob, probabilityExponent); 
    }   
    
   @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturn(Station s, double arrivaloffset, double checkintervall) {
        return null;
    }
   @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRent(Station s, double arrivaloffset, double checkintervall) {
        return null;
    }
   @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutReturnSurrounding(Station s, double arrivaloffset, double checkintervall, double maxdistancesurrounding, List<Station> allStations) {
        return null;
    }
   @Override
    public ExpectedUnsuccessData calculateExpectedFutureFailsWithAndWithoutRentSurrounding(Station s, double arrivaloffset, double checkintervall, double maxdistancesurrounding, List<Station> allStations) {
        return null;
    }
    //methods for calculation probabilities    
    //calculates the probabilities of taking or returning bikes at a station at the moment 
    //currenttime+predictionoffset,
    // if (or if not) a bike is taken/returned at time currenttime+arrivaloffset
    // arrivaltime may be before or after the predictioninterval
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station s, double arrivaloffset,double predictionoffset) {

        //here arrivaloffset is ignored (the possible takes/returns take place at currenttime+predictionoffset)
        //probabilities are calculated from now to now+predictionoffset
        // that is, it is simplified that the bike is taken/returned at currenttime+predictionoffset  
        IntTuple avCB=getAvailableCapandBikes( s,  predictionoffset);
        
        ProbabilityData pd=new ProbabilityData();

        int estimatedbikes = avCB.avbikes + avCB.minpostchanges- additionalResourcesDesiredInProbability;
        int estimatedslots = (avCB.avslots - avCB.maxpostchanges)- additionalResourcesDesiredInProbability;

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.returndemandrate, avCB.takedemandrate, k);
        pd.probabilityTakeAfterTake = pd.probabilityTake - ProbabilityDistributions.calculateSkellamProbability(avCB.returndemandrate, avCB.takedemandrate, k);
        k = k - 1;
        pd.probabilityTakeAfterRerturn = pd.probabilityTake + ProbabilityDistributions.calculateSkellamProbability(avCB.returndemandrate, avCB.takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.takedemandrate, avCB.returndemandrate, k);
        pd.probabilityReturnAfterReturn = pd.probabilityReturn - ProbabilityDistributions.calculateSkellamProbability(avCB.takedemandrate, avCB.returndemandrate, k);
        k = k - 1;
        pd.probabilityReturnAfterTake = pd.probabilityReturn + ProbabilityDistributions.calculateSkellamProbability(avCB.takedemandrate, avCB.returndemandrate, k);
        
        pd.probabilityTake=Math.pow(pd.probabilityTake,probabilityExponent);
        pd.probabilityTakeAfterTake=Math.pow(pd.probabilityTakeAfterTake,probabilityExponent);
        pd.probabilityReturnAfterTake=Math.pow(pd.probabilityReturnAfterTake,probabilityExponent);
        pd.probabilityReturn=Math.pow(pd.probabilityReturn,probabilityExponent);
        pd.probabilityTakeAfterRerturn=Math.pow(pd.probabilityTakeAfterRerturn,probabilityExponent);
        pd.probabilityReturnAfterReturn=Math.pow(pd.probabilityReturnAfterReturn,probabilityExponent);

        return pd;
   }

    // this case is the same as the one before, but we assume  that a bike is taken /returned at attime
    // That is, we calculate the probabilities of taking or returning bikes at a station at the moment 
    //currenttime+attime, if (or if not) a bike is taken/returned at time currenttime+atime
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station sd, double predictionoffset){
        return calculateFutureProbabilitiesWithAndWithoutArrival(sd, predictionoffset,predictionoffset);
    }
    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double fromtime,double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double fromtime,double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateExpectedTakes(Station s, double fromtime,double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(takedemandrate, returndemandrate, 1);
    }
    public double calculateExpectedReturns(Station s, double fromtime,double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double fromtime,double duration) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double fromtime,double duration) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), duration);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(returndemandrate, 1);
    }
   
    public double getGlobalProbabilityImprovementIfTake(StationData sd ) {
        int timeoffset=(int)sd.walktime;
        double futtakedemand = dm.getStationTakeRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.probabilityTakeAfterTake-sd.probabilityTake)
                + (futreturndemand / futglobalretdem) * 
                (sd.probabilityReturnAfterTake-sd.probabilityReturn);
        return relativeimprovemente;
    }

    public double getGlobalProbabilityImprovementIfReturn(StationData sd) {
        int timeoffset =(int) sd.biketime;
        double futtakedemand = dm.getStationTakeRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.station.getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.probabilityTakeAfterRerturn-sd.probabilityTake)
                + (futreturndemand / futglobalretdem) * 
                (sd.probabilityReturnAfterReturn-sd.probabilityReturn);
        return relativeimprovemente;
    }
}
