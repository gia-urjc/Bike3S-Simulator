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
public class UtilitiesProbabilityCalculation {
  
    public class ProbabilityData{
        public double probabilityTake;
        public double probabilityReturn;
        public double probabilityTakeAfterTake;
        public double probabilityReturnAfterTake;
        public double probabilityTakeAfterRerturn;
        public double probabilityReturnAfterReturn;
    }

    final DemandManager dm;
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    
    public UtilitiesProbabilityCalculation(DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
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
        double probbike = SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, k);
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
        double probslot = SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, k);
        return probslot;
    }
    //methods for calculation probabilities    
    public ProbabilityData calculateAllTakeProbabilities(StationUtilityData sd, double timeoffset) {
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
        pd.probabilityTake = SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, k);
        pd.probabilityTakeAfterTake = pd.probabilityTake - SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, k);
        k = k - 1;
        pd.probabilityReturnAfterTake = pd.probabilityReturn + SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;
    }

     //methods for calculation probabilities    
    public ProbabilityData calculateAllReturnProbabilities(StationUtilityData sd, double timeoffset) {
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
        pd.probabilityTake = SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, k);
        k = k - 1;
        pd.probabilityTakeAfterRerturn = pd.probabilityTake + SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, k);
        pd.probabilityReturnAfterReturn = pd.probabilityReturn - SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;

    }
   
    //methods for calculation probabilities    
    public ProbabilityData calculateAllProbabilities(StationUtilityData sd, double timeoffset) {
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
        pd.probabilityTake = SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, k);
        pd.probabilityTakeAfterTake = pd.probabilityTake - SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);
        k = k - 1;
        pd.probabilityTakeAfterRerturn = pd.probabilityTake + SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        pd.probabilityReturn = SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, k);
        pd.probabilityReturnAfterReturn = pd.probabilityReturn - SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);
        k = k - 1;
        pd.probabilityReturnAfterTake = pd.probabilityReturn + SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        return pd;
     }
    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return SellamDistribution.calculateCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double timeoffset) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return SellamDistribution.calculateCDFPoissonProbability(returndemandrate, 1);
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
