/*
Here probability is calculatest as follows:
P(finding a bike in sercain time)=P(x>=k|knownfuturebikes) where k=1-currentbikes
where knownfuturebike are the expected bikes in future
The probability is calculated through skellam
That is here, expected bikes in the futer (or takes of bikes) are treated as conditionals for the probability calculation

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.util.ProbabilityDistributions;
import static es.urjc.ia.bikesurbanfleets.common.util.ProbabilityDistributions.conditionalUpCDFSkellamProbability;
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
        int availablebikesestimated = currentbikes- additionalResourcesDesiredInProbability;
        int knownbikestocome=estimatedbikechanges+compromisedbikes;
        int k = 1 - availablebikesestimated;
        double probbike = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k,knownbikestocome);  
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
        int availableslotsestimated = currentslots- additionalResourcesDesiredInProbability;
        int knownslotstocome=-estimatedbikechanges-compromisedslots;
        int k = 1 - availableslotsestimated;
        double probslot = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome);  
        return probslot;
    }
    //methods for calculation probabilities    
    public ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), futureinstant);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes-additionalResourcesDesiredInProbability;
        int knownbikestocome=estimatedbikechanges+compromisedbikes;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome);
        pd.probabilityTakeAfterTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome-1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- additionalResourcesDesiredInProbability;
        int knownslotstocome=-estimatedbikechanges-compromisedslots;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome);
        pd.probabilityReturnAfterTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome+1);

        return pd;
    }

    //methods for calculation probabilities    
    public ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), futureinstant);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes-additionalResourcesDesiredInProbability;
        int knownbikestocome=estimatedbikechanges+compromisedbikes;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome);
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome+1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- additionalResourcesDesiredInProbability;
        int knownslotstocome=-estimatedbikechanges-compromisedslots;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome-1);

        return pd;
    }
 
    //methods for calculation probabilities    
    public ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        ProbabilityData pd=new ProbabilityData();
        Station s = sd.getStation();
        int currentbikes = s.availableBikes();
        int currentslots = s.availableSlots();
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;

        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), futureinstant);
            estimatedbikechanges += (int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                compromisedbikes += (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                compromisedslots -= (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), futureinstant);

        //probability that a bike exists and that is exists after taking one 
        int availablebikesestimated = currentbikes-additionalResourcesDesiredInProbability;
        int knownbikestocome=estimatedbikechanges+compromisedbikes;
        int k = 1 - availablebikesestimated;
        pd.probabilityTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome);
        pd.probabilityTakeAfterTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome-1);
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(returndemandrate, takedemandrate, k, knownbikestocome+1);

        //probability that a slot exists and that is exists after taking one 
        int availableslotsestimated = currentslots- additionalResourcesDesiredInProbability;
        int knownslotstocome=-estimatedbikechanges-compromisedslots;
        k = 1 - availableslotsestimated;
        pd.probabilityReturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome-1);
        pd.probabilityReturnAfterTake = ProbabilityDistributions.conditionalUpCDFSkellamProbability(takedemandrate, returndemandrate, k,knownslotstocome+1);
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
