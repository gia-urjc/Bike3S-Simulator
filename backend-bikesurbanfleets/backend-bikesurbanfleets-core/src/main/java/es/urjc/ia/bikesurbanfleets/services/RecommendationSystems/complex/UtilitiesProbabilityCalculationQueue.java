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
  
    final private double maxAverageDistanceToStationForTake=200D;
    final private double squaredMaxAverageDistanceToStationForTake=maxAverageDistanceToStationForTake*maxAverageDistanceToStationForTake;
    final private double maxAverageDistanceToStationForReturn=6000D;
    final private double squaredMaxAverageDistanceToStationForReturn=maxAverageDistanceToStationForReturn*maxAverageDistanceToStationForReturn;
    final private double probabilityUsersObey ;
    final private boolean takeintoaccountexpected ;
    final private boolean takeintoaccountcompromised ;
    final private PastRecommendations pastrecs;
    final private int additionalResourcesDesiredInProbability;
    final public static double h=0.05D;
    final private double probabilityExponent;
    
    public UtilitiesProbabilityCalculationQueue(double probabilityExponent,DemandManager dm, PastRecommendations pastrecs, double probabilityUsersObey,
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

    @Override
    public ProbabilityData calculateFutureTakeProbabilitiesWithArrival(Station sd, double fromtime,double totime) {
        return calculateFutureAllProbabilitiesWithArrival(sd,  fromtime, totime);
    }

    @Override
    public ProbabilityData calculateFutureReturnProbabilitiesWithArrival(Station sd, double fromtime,double totime) {
         return calculateFutureAllProbabilitiesWithArrival(sd, fromtime, totime);
    }

    class IntTuple{
        int avcap;
        int avslots;
        int avbikes;
        int minpostchanges=0;
        int maxpostchanges=0;
        double takedemandrate;
        double returndemandrate;
    }
    // get current capacity and available bikes
    // this takes away reserved bikes and slots and takes into account expected changes
    public IntTuple getAvailableCapandBikes(Station s, double fromtime,double totime) {
        IntTuple res =new IntTuple();
        res.avcap=s.getCapacity()-s.getReservedBikes()-s.getReservedSlots();
        res.avbikes=s.availableBikes();
        res.avslots=res.avcap-res.avbikes;
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), totime);
            res.avbikes = res.avbikes + (int) Math.floor(er.changes * probabilityUsersObey);
            res.avslots = res.avslots - (int) Math.floor(er.changes * probabilityUsersObey);
            //normalize just in case
            if (takeintoaccountcompromised) {
                res.maxpostchanges=(int) er.maxpostchanges ;
                res.minpostchanges=(int) er.minpostchanges ;
            }
        }
        if (res.avbikes<0) res.avbikes=0;
        if (res.avbikes>res.avcap) res.avbikes=res.avcap;
        if (res.avslots<0) res.avslots=0;
        if (res.avslots>res.avcap) res.avslots=res.avcap;
        res.takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);        
        return res;
    }

    // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  0, timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avbikes<=-avCB.minpostchanges && avCB.minpostchanges<0) {
 //           return 0;
 //       }
        int initialbikes=avCB.avbikes;//+avCB.minpostchanges;
        initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,initialbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability-avCB.minpostchanges;
        double prob=pc.kOrMoreBikesProbability(requiredbikes);
        return Math.pow(prob, probabilityExponent); 
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  0, timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avslots<=avCB.maxpostchanges && avCB.maxpostchanges>0) {
//            return 0;
 //       }
        int initialbikes=avCB.avcap-(avCB.avslots);//-avCB.maxpostchanges);
        initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,initialbikes); 
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;
        double prob=pc.kOrMoreSlotsProbability(requiredslots); 
        return Math.pow(prob, probabilityExponent); 
     }
   
    //methods for calculation probabilities    
    public ProbabilityData calculateFutureAllProbabilitiesWithArrival(Station s, double fromtime,double totime) {
        ProbabilityData pd=new ProbabilityData();
        IntTuple avCB=getAvailableCapandBikes( s,   fromtime, totime);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avbikes<=-avCB.minpostchanges && avCB.minpostchanges<0) {
 //           return 0;
 //       }
        int initialbikes=avCB.avbikes;
        initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,1,initialbikes);     

        int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;        
        pd.probabilityTake= Math.pow(pc.kOrMoreBikesProbability(requiredbikes), probabilityExponent); 
        pd.probabilityTakeAfterTake = Math.pow(pc.kOrMoreBikesProbability(requiredbikes+1), probabilityExponent);
        pd.probabilityReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots), probabilityExponent);        
        pd.probabilityReturnAfterReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots+1), probabilityExponent);
        pd.probabilityTakeAfterRerturn = Math.pow(pc.kOrMoreBikesProbability(requiredbikes-1), probabilityExponent); 
        pd.probabilityReturnAfterTake =Math.pow( pc.kOrMoreSlotsProbability(requiredslots-1), probabilityExponent);
        return pd;
    }
    
    //methods for calculation probabilities    
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double fromtime,double totime) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(takedemandrate, returndemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double fromtime,double totime) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        return ProbabilityDistributions.calculateUpCDFSkellamProbability(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateExpectedTakes(Station s, double fromtime,double totime) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(takedemandrate, returndemandrate, 1);
    }
    public double calculateExpectedReturns(Station s, double fromtime,double totime) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        return ProbabilityDistributions.calculateUpCDFSkellamProbabilityTimesNumer(returndemandrate, takedemandrate, 1);
    }

    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(Station s, double fromtime,double totime) {
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
        return ProbabilityDistributions.calculateUpCDFPoissonProbability(takedemandrate, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(Station s, double fromtime,double totime) {
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtime), totime);
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
