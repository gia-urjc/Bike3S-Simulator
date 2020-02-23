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

   // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        int estimatedbikes = s.availableBikes();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                estimatedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
             }
        }
        
        estimatedbikes -=additionalResourcesDesiredInProbability;
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double probbike = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, k);
        return probbike;
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedslots -= (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        estimatedslots -=additionalResourcesDesiredInProbability;
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a slot exists and that is exists after taking one 
        int k = 1 - estimatedslots;
        double probslot = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, k);
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
