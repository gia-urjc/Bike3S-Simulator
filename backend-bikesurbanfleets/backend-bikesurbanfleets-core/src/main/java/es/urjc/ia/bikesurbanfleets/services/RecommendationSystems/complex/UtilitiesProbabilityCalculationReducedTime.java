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
import java.time.LocalDateTime;

/**
 *
 * @author holger
 */
public class UtilitiesProbabilityCalculationReducedTime extends UtilitiesProbabilityCalculator{
  
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    
    public UtilitiesProbabilityCalculationReducedTime(DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
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

    private class Helper{
        int estimatedbikechanges=0;
        int compromisedbikes=0;
        int compromisedslots=0;
        LocalDateTime lastknownChangeTime;
        long newoffset;
    }
    
    private Helper getExpectedChanges(Station s, long timeoffset){
        Helper h= new Helper();
        long lastknownChangeInstant=SimulationDateTime.getCurrentSimulationInstant();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            h.estimatedbikechanges = (int) Math.floor(er.changes * probabilityUsersObey);
            if (er.lastendinstantexpected>lastknownChangeInstant) {
                lastknownChangeInstant=er.lastendinstantexpected;
            }
            if (takeintoaccountcompromised) {
                h.compromisedbikes = (int) Math.floor(er.minpostchanges * probabilityUsersObey);
                h.compromisedslots = (int) Math.floor(er.maxpostchanges * probabilityUsersObey);
            }
        }
        h.lastknownChangeTime=SimulationDateTime.getSimulationDateTimeFromOffset(lastknownChangeInstant);
        h.newoffset=(SimulationDateTime.getCurrentSimulationInstant()+timeoffset)-lastknownChangeInstant;
        return h;
    }
    // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        Helper h=getExpectedChanges(s,(long)timeoffset);
        //get the demandrate form the last known event to current+offsettime
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);

        //probability that a bike exists and that is exists after taking one 
        int currentbikes = s.availableBikes();
        int futureestimatedbikes = currentbikes + h.estimatedbikechanges + h.compromisedbikes - additionalResourcesDesiredInProbability;
        int k = 1 - futureestimatedbikes;
        double probbike = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, k);
        return probbike;
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        Helper h=getExpectedChanges(s,(long)timeoffset);
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);

        //probability that a slot exists and that is exists after taking one 
        int currentslots = s.availableSlots();
        int futureestimatedslots = currentslots - h.estimatedbikechanges - h.compromisedslots - additionalResourcesDesiredInProbability;
        int k = 1 - futureestimatedslots;
        double probslot = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, k);
        return probslot;
    }
    //methods for calculation probabilities    
    public UtilitiesProbabilityCalculator.ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        UtilitiesProbabilityCalculator.ProbabilityData pd=new UtilitiesProbabilityCalculator.ProbabilityData();
        Station s = sd.getStation();
        
        Helper h=getExpectedChanges(s,(long)futureinstant);
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        
        int currentbikes = s.availableBikes();
        int futureestimatedbikes = currentbikes + h.estimatedbikechanges + h.compromisedbikes - additionalResourcesDesiredInProbability;
        //probability that a bike exists and that is exists after taking one 
        int kt = 1 - futureestimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt);
        //probability that a slot exists and that is exists after taking one 
        int currentslots = s.availableSlots();
        int futureestimatedslots = currentslots - h.estimatedbikechanges - h.compromisedslots - additionalResourcesDesiredInProbability;
        int kr = 1 - futureestimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr);

        // now calculate the posterior probabilities if the take takes place
        double offsetlastexpected=futureinstant-h.newoffset;
        if (offsetlastexpected<0 || offsetinstantArrivalCurrent<0) throw new RuntimeException("imposible path");
        if(offsetinstantArrivalCurrent>offsetlastexpected && offsetinstantArrivalCurrent<=futureinstant){
            h.lastknownChangeTime=SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(offsetinstantArrivalCurrent);
            h.newoffset=futureinstant-offsetinstantArrivalCurrent;
            takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
            returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        }
        pd.probabilityTakeAfterTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt+1);
        pd.probabilityReturnAfterTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr-1);
        return pd;
    }

     //methods for calculation probabilities    
    public UtilitiesProbabilityCalculator.ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        UtilitiesProbabilityCalculator.ProbabilityData pd=new UtilitiesProbabilityCalculator.ProbabilityData();
        Station s = sd.getStation();
        
        Helper h=getExpectedChanges(s,(long)futureinstant);
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);

        int currentbikes = s.availableBikes();
        int futureestimatedbikes = currentbikes + h.estimatedbikechanges + h.compromisedbikes - additionalResourcesDesiredInProbability;
        //probability that a bike exists and that is exists after taking one 
        int kt = 1 - futureestimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt);
        //probability that a slot exists and that is exists after taking one 
        int currentslots = s.availableSlots();
        int futureestimatedslots = currentslots - h.estimatedbikechanges - h.compromisedslots - additionalResourcesDesiredInProbability;
        int kr = 1 - futureestimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr);

        // now calculate the posterior probabilities if the return takes place
        double offsetlastexpected=futureinstant-h.newoffset;
        if (offsetlastexpected<0 || offsetinstantArrivalCurrent<0) throw new RuntimeException("imposible path");
        if(offsetinstantArrivalCurrent>offsetlastexpected && offsetinstantArrivalCurrent<=futureinstant){
            h.lastknownChangeTime=SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(offsetinstantArrivalCurrent);
            h.newoffset=futureinstant-offsetinstantArrivalCurrent;
            takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
            returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        }
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt-1);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr+1);
        return pd;
    }
    //methods for calculation probabilities    
    public UtilitiesProbabilityCalculator.ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, long offsetinstantArrivalCurrent, long futureinstant) {
        UtilitiesProbabilityCalculator.ProbabilityData pd=new UtilitiesProbabilityCalculator.ProbabilityData();
        Station s = sd.getStation();
        
        Helper h=getExpectedChanges(s,(long)futureinstant);
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);

        int currentbikes = s.availableBikes();
        int futureestimatedbikes = currentbikes + h.estimatedbikechanges + h.compromisedbikes - additionalResourcesDesiredInProbability;
        //probability that a bike exists and that is exists after taking one 
        int kt = 1 - futureestimatedbikes;
        pd.probabilityTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt);
        //probability that a slot exists and that is exists after taking one 
        int currentslots = s.availableSlots();
        int futureestimatedslots = currentslots - h.estimatedbikechanges - h.compromisedslots - additionalResourcesDesiredInProbability;
        int kr = 1 - futureestimatedslots;
        pd.probabilityReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr);

        // now calculate the posterior probabilities if the return takes place
        double offsetlastexpected=futureinstant-h.newoffset;
        if (offsetlastexpected<0 || offsetinstantArrivalCurrent<0) throw new RuntimeException("imposible path");
        if(offsetinstantArrivalCurrent>offsetlastexpected && offsetinstantArrivalCurrent<=futureinstant){
            h.lastknownChangeTime=SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(offsetinstantArrivalCurrent);
            h.newoffset=futureinstant-offsetinstantArrivalCurrent;
            takedemandrate = dm.getStationTakeRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
            returndemandrate = dm.getStationReturnRateIntervall(s.getId(), h.lastknownChangeTime, h.newoffset);
        }
        pd.probabilityTakeAfterRerturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt-1);
        pd.probabilityReturnAfterReturn = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr+1);
        pd.probabilityTakeAfterTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, kt+1);
        pd.probabilityReturnAfterTake = ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, kr-1);
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
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double timeoffset) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double timeoffset) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(returndemandrate, 1);
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
