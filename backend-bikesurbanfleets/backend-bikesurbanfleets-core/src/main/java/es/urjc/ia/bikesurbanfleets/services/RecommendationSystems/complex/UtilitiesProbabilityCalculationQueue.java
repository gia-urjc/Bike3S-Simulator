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
import es.urjc.ia.bikesurbanfleets.common.util.StationProbabilitiesQueueBased;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author holger
 */
public class UtilitiesProbabilityCalculationQueue extends UtilitiesProbabilityCalculator{
  
    final private double maxAverageDistanceToStationForTake=500D;
    final private double squaredMaxAverageDistanceToStationForTake=maxAverageDistanceToStationForTake*maxAverageDistanceToStationForTake;
    final private double maxAverageDistanceToStationForReturn=3000D;
    final private double squaredMaxAverageDistanceToStationForReturn=maxAverageDistanceToStationForReturn*maxAverageDistanceToStationForReturn;
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    final private double h=0.05D;
    
    public UtilitiesProbabilityCalculationQueue(DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
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
        int avbikes;
        int minpostchanges=0;
        int maxpostchanges=0;
        double takedemandrate;
        double returndemandrate;
    }
    // get current capacity and available bikes
    // this takes away reserved bikes and slots and takes into account expected changes
    private IntTuple getAvailableCapandBikes(Station s, double timeoffset) {
        IntTuple res =new IntTuple();
        double reductionfactortake=1d;
        double reductionfactorreturn=1d;
        long lastKnownEventTime=SimulationDateTime.getCurrentSimulationInstant();;
        res.avcap=s.getCapacity()-s.getReservedBikes()-s.getReservedSlots();
        res.avbikes=s.availableBikes();
        if (takeintoaccountexpected) {
            reductionfactortake=(timeoffset*timeoffset)/squaredMaxAverageDistanceToStationForTake;
            reductionfactorreturn=(timeoffset*timeoffset)/squaredMaxAverageDistanceToStationForReturn;
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            res.avbikes += (int) Math.floor(er.changes * probabilityUsersObey);
            lastKnownEventTime=er.lastendinstantexpected;
            //normalize just in case
            if (res.avbikes<0) res.avbikes=0;
            if (res.avbikes>res.avcap) res.avbikes=res.avcap;
            if (takeintoaccountcompromised) {
                res.maxpostchanges=(int) Math.floor(er.maxpostchanges * probabilityUsersObey);
                res.minpostchanges=(int) Math.floor(er.minpostchanges * probabilityUsersObey);
            }
        }
        reductionfactortake=Math.min(reductionfactortake,1D);
        reductionfactorreturn=Math.min(reductionfactorreturn, 1D);
        double redfactor=1D;//Math.min(reductionfactorreturn,reductionfactortake);
        long addseconds=Math.max(0, lastKnownEventTime-SimulationDateTime.getCurrentSimulationInstant());
     //   System.out.println("station " + s.getId()+ " time " + timeoffset+ " reductionfactortake " + reductionfactortake+ " reductionfactorreturn "+ reductionfactortake);
        res.takedemandrate = redfactor*dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        res.returndemandrate = redfactor*dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);        
   //     LocalDateTime lastdatetime=SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(addseconds);
   //     res.takedemandrate = redfactor*dm.getStationTakeRateIntervall(s.getId(), lastdatetime, timeoffset);
   //     res.returndemandrate = redfactor*dm.getStationReturnRateIntervall(s.getId(), lastdatetime, timeoffset);        

        return res;
    }

    // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,avCB.avbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
        return pc.kOrMoreBikesProbability(requiredbikes); 
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,avCB.avbikes); 
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;
        return pc.kOrMoreSlotsProbability(requiredslots); 
    }
    //methods for calculation probabilities    
    public ProbabilityData calculateAllTakeProbabilitiesWithArrival(StationUtilityData sd,double timeoffset) {
        Station s = sd.getStation();
        ProbabilityData pd=new ProbabilityData();
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,avCB.avbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;        
        pd.probabilityTake = pc.kOrMoreBikesProbability(requiredbikes);
        pd.probabilityTakeAfterTake = pc.kOrMoreBikesProbability(requiredbikes+1);
        pd.probabilityReturn = pc.kOrMoreSlotsProbability(requiredslots);        
        pd.probabilityReturnAfterTake = pc.kOrMoreSlotsProbability(requiredslots-1);
        return pd;
    }

     //methods for calculation probabilities    
    public ProbabilityData calculateAllReturnProbabilitiesWithArrival(StationUtilityData sd, double timeoffset) {
        Station s = sd.getStation();
        ProbabilityData pd=new ProbabilityData();
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,avCB.avbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;        
        pd.probabilityTake = pc.kOrMoreBikesProbability(requiredbikes);
        pd.probabilityTakeAfterRerturn = pc.kOrMoreBikesProbability(requiredbikes-1);
        pd.probabilityReturn = pc.kOrMoreSlotsProbability(requiredslots);        
        pd.probabilityReturnAfterReturn = pc.kOrMoreSlotsProbability(requiredslots+1);
        return pd;
    }
   
    //methods for calculation probabilities    
    public ProbabilityData calculateAllProbabilitiesWithArrival(StationUtilityData sd, double timeoffset) {
        Station s = sd.getStation();
        ProbabilityData pd=new ProbabilityData();
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,avCB.avbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;        
        pd.probabilityTake = pc.kOrMoreBikesProbability(requiredbikes);
        pd.probabilityTakeAfterRerturn = pc.kOrMoreBikesProbability(requiredbikes-1);
        pd.probabilityTakeAfterTake = pc.kOrMoreBikesProbability(requiredbikes+1);
        pd.probabilityReturn = pc.kOrMoreSlotsProbability(requiredslots);        
        pd.probabilityReturnAfterReturn = pc.kOrMoreSlotsProbability(requiredslots+1);
        pd.probabilityReturnAfterTake = pc.kOrMoreSlotsProbability(requiredslots-1);
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
