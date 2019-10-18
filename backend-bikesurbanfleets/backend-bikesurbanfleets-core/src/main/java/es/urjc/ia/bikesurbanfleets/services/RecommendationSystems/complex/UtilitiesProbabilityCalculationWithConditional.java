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
public class UtilitiesProbabilityCalculationWithConditional extends UtilitiesProbabilityCalculator{
  
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    
    public UtilitiesProbabilityCalculationWithConditional(DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
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
        int currentbikes = s.availableBikes();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikechanges = (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                compromisedbikes = (int) Math.floor(er.minpostchanges * probabilityUsersObey);
             }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes+ estimatedbikechanges+ compromisedbikes;
/*        if (availablebikesestimated<0 || availablebikesestimated>s.getCapacity())
            System.out.println("situacion rara: station: " + s.getId() + " capacity: " + s.getCapacity()+ 
                    " currentbikes: " + currentbikes + " estimatedchange: " + estimatedbikechanges + 
                    " compromisedbikes: "+ compromisedbikes + " total: " + availablebikesestimated);
  */    availablebikesestimated-= additionalResourcesDesiredInProbability;
        int k = 1 - availablebikesestimated;
        double probbike = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k,estimatedbikechanges);  
        return probbike;
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedslots=0;
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikechanges = (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedslots = (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- estimatedbikechanges - compromisedslots;
  /*      if (availableslotsestimated<0 || availableslotsestimated>s.getCapacity())
            System.out.println("situacion rara: station: " + s.getId() + " capacity: " + s.getCapacity()+ 
                    " currentslots: " + currentslots + " estimatedchange: " + estimatedbikechanges + 
                    " compromisedslots: "+ compromisedslots + " total: " + availableslotsestimated);
    */  availableslotsestimated-= additionalResourcesDesiredInProbability;
        int k = 1 - availableslotsestimated;
        double probslot = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k,-estimatedbikechanges);  
        return probslot;
    }
    //methods for calculation probabilities    
    public ProbabilityData calculateAllTakeProbabilities(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes+ estimatedbikechanges+ compromisedbikes;
        availablebikesestimated-= additionalResourcesDesiredInProbability;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k, estimatedbikechanges);
        pd.probabilityTakeAfterTake = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k+1, estimatedbikechanges-1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- estimatedbikechanges - compromisedslots;
        availableslotsestimated-= additionalResourcesDesiredInProbability;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k,-estimatedbikechanges);
        pd.probabilityReturnAfterTake = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k-1,-(estimatedbikechanges-1));

        return pd;
    }

    //methods for calculation probabilities    
    public ProbabilityData calculateAllReturnProbabilities(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes+ estimatedbikechanges+ compromisedbikes;
        availablebikesestimated-= additionalResourcesDesiredInProbability;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k, estimatedbikechanges);
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k-1, estimatedbikechanges+1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- estimatedbikechanges - compromisedslots;
        availableslotsestimated-= additionalResourcesDesiredInProbability;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k,-estimatedbikechanges);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k+1,-(estimatedbikechanges+1));

        return pd;
    }
 
    //methods for calculation probabilities    
    public ProbabilityData calculateAllProbabilities(StationUtilityData sd, double timeoffset) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes+ estimatedbikechanges+ compromisedbikes;
        availablebikesestimated-= additionalResourcesDesiredInProbability;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k, estimatedbikechanges);
        pd.probabilityTakeAfterTake = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k+1, estimatedbikechanges-1);
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.conditionalCDFSkellamProbability(returndemandrate, takedemandrate, k-1, estimatedbikechanges+1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- estimatedbikechanges - compromisedslots;
        availableslotsestimated-= additionalResourcesDesiredInProbability;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k,-estimatedbikechanges);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k+1,-(estimatedbikechanges+1));
        pd.probabilityReturnAfterTake = ProbabilityDistributions.conditionalCDFSkellamProbability(takedemandrate, returndemandrate, k-1,-(estimatedbikechanges-1));
        return pd;
     }
    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double timeoffset) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateCDFPoissonProbability(returndemandrate, 1);
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
