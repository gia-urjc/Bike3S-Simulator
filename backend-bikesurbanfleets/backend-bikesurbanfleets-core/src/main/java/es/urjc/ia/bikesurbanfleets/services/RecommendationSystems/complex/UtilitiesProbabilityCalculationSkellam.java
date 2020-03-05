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
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

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
    
    public UtilitiesProbabilityCalculationSkellam(DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
            boolean takeintoaccountexpected, boolean takeintoaccountcompromised, int additionalResourcesDesiredInProbability
    ) {
        this.dm = dm;
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
             PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
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
        if (avCB.avbikes <=-avCB.minpostchanges && avCB.minpostchanges<0) {
            return 0;
        }
        int estimatedbikes = avCB.avbikes + avCB.minpostchanges- additionalResourcesDesiredInProbability;
        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double probbike = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.returndemandrate, avCB.takedemandrate, k);
        return probbike;
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        //if there are other demands already registered, dont' take the last bike
        if (avCB.avslots <=avCB.maxpostchanges && avCB.maxpostchanges>0) {
            return 0;
        }
        int estimatedslots=(avCB.avslots - avCB.maxpostchanges)- additionalResourcesDesiredInProbability;
        //probability that a slot exists and that is exists after taking one 
        int k = 1 - estimatedslots;
        double probslot = ProbabilityDistributions.calculateUpCDFSkellamProbability(avCB.takedemandrate, avCB.returndemandrate, k);
        return probslot;
    }
    //methods for calculation probabilities    
    public ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            estimatedslots -= (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        estimatedbikes -=additionalResourcesDesiredInProbability;
        estimatedslots -=additionalResourcesDesiredInProbability;
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that isexists after taking one 
        int k = 1 - estimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, k);
        pd.probabilityTakeAfterTake = pd.probabilityTake - ProbabilityDistributions.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, k);
        k = k - 1;
        pd.probabilityReturnAfterTake = pd.probabilityReturn + ProbabilityDistributions.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;
    }

     //methods for calculation probabilities    
    public ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            estimatedslots -= (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        estimatedbikes -=additionalResourcesDesiredInProbability;
        estimatedslots -=additionalResourcesDesiredInProbability;
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, k);
        k = k - 1;
        pd.probabilityTakeAfterRerturn = pd.probabilityTake + ProbabilityDistributions.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, k);
        pd.probabilityReturnAfterReturn = pd.probabilityReturn - ProbabilityDistributions.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;

    }
   
    //methods for calculation probabilities    
    public ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            estimatedslots -= (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        estimatedbikes -=additionalResourcesDesiredInProbability;
        estimatedslots -=additionalResourcesDesiredInProbability;
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, k);
        pd.probabilityTakeAfterTake = pd.probabilityTake - ProbabilityDistributions.calculateSkellamProbability(returndemandrate, takedemandrate, k);
        k = k - 1;
        pd.probabilityTakeAfterRerturn = pd.probabilityTake + ProbabilityDistributions.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, k);
        pd.probabilityReturnAfterReturn = pd.probabilityReturn - ProbabilityDistributions.calculateSkellamProbability(takedemandrate, returndemandrate, k);
        k = k - 1;
        pd.probabilityReturnAfterTake = pd.probabilityReturn + ProbabilityDistributions.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;
     }
    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateExpectedTakes(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(takedemandrate, returndemandrate, 1);
    }
    public double calculateExpectedReturns(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double timeoffset) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(returndemandrate, 1);
    }
   
    public double getGlobalProbabilityImprovementIfTake(StationUtilityData sd ) {
        int timeoffset=(int)sd.getWalkTime();
        double futtakedemand = dm.getStationTakeRateIntervall(sd.getStation().getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.getStation().getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.getProbabilityTakeAfterTake()-sd.getProbabilityTake())
                + (futreturndemand / futglobalretdem) * 
                (sd.getProbabilityReturnAfterTake()-sd.getProbabilityReturn());
        return relativeimprovemente;
    }

    public double getGlobalProbabilityImprovementIfReturn(StationUtilityData sd) {
        int timeoffset =(int) sd.getBiketime();
        double futtakedemand = dm.getStationTakeRateIntervall(sd.getStation().getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futreturndemand = dm.getStationReturnRateIntervall(sd.getStation().getId(), SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobaltakedem = dm.getGlobalTakeRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);
        double futglobalretdem = dm.getGlobalReturnRateIntervall(SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(timeoffset), 3600);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.getProbabilityTakeAfterRerturn()-sd.getProbabilityTake())
                + (futreturndemand / futglobalretdem) * 
                (sd.getProbabilityReturnAfterReturn()-sd.getProbabilityReturn());
        return relativeimprovemente;
    }
}
