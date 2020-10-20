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

    class IntTuple{
        int avcap;
        int avslots;
        int avbikes;
        int minpostchanges=0;
        int maxpostchanges=0;
        double takedemandrate;
        double returndemandrate;
        int changesInTime=0;
    }
    // get current capacity and available bikes
    // this takes away reserved bikes and slots and takes into account expected changes
    public IntTuple getAvailableCapandBikes(Station s, double timeoffset) {
        IntTuple res =new IntTuple();
        res.avcap=s.getCapacity()-s.getReservedBikes()-s.getReservedSlots();
        res.avbikes=s.availableBikes();
        res.avslots=res.avcap-res.avbikes;
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), 0, timeoffset);
            res.avbikes = res.avbikes + (int) Math.floor(er.changes * probabilityUsersObey);
            res.avslots = res.avslots - (int) Math.floor(er.changes * probabilityUsersObey);
            res.changesInTime=(int) Math.floor(er.changes * probabilityUsersObey);
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
                SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);        
        return res;
    }
    
    private IntTuple getChangesInIntervall(Station s, double fromtimeoffset, double totimeoffset) {
        if (totimeoffset<fromtimeoffset || fromtimeoffset<0) throw new RuntimeException("error in data");
        IntTuple res =new IntTuple();
        if (takeintoaccountexpected) {
            PastRecommendations.ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), fromtimeoffset, totimeoffset);
            res.changesInTime=(int) Math.floor(er.changes * probabilityUsersObey);
            if (takeintoaccountcompromised) {
                res.maxpostchanges=(int) er.maxpostchanges ;
                res.minpostchanges=(int) er.minpostchanges ;
            }
        }
        res.takedemandrate = dm.getStationTakeRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtimeoffset), totimeoffset-fromtimeoffset);
        res.returndemandrate = dm.getStationReturnRateIntervall(s.getId(), 
                SimulationDateTime.getCurrentSimulationDateTime().plusSeconds((int)fromtimeoffset), totimeoffset-fromtimeoffset);        
        return res;
    }

    // Probabilities form now to timeoffset 
    public double calculateTakeProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avbikes<=-avCB.minpostchanges && avCB.minpostchanges<0) {
 //           return 0;
 //       }
        int initialbikes=avCB.avbikes;//+avCB.minpostchanges;
        initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,initialbikes);     
        int requiredbikes=1+ additionalResourcesDesiredInProbability-avCB.minpostchanges;
        double prob=pc.kOrMoreBikesProbability(requiredbikes);
        return Math.pow(prob, probabilityExponent); 
    }
    public double calculateReturnProbability(Station s, double timeoffset) {
        IntTuple avCB=getAvailableCapandBikes( s,  timeoffset);
        //if there are other demands already registered, dont' take the last bike
//        if (avCB.avslots<=avCB.maxpostchanges && avCB.maxpostchanges>0) {
//            return 0;
 //       }
        int initialbikes=avCB.avcap-(avCB.avslots);//-avCB.maxpostchanges);
        initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
        StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,initialbikes); 
        int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;
        double prob=pc.kOrMoreSlotsProbability(requiredslots); 
        return Math.pow(prob, probabilityExponent); 
     }
   
    //methods for calculation probabilities    
    //calculates the probabilities of taking or returning bikes at a station at the moment 
    //currenttime+predictionoffset,
    // if (or if not) a bike is taken/returned at time currenttime+arrivaloffset
    // arrivaltime may be before or after the predictioninterval
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station s, double arrivaloffset,double predictionoffset) {

        ProbabilityData pd=new ProbabilityData();
        
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // now there are two different cases
        //case1: predictionoffset<arrivaloffset
        //     here we consider the take/return like q compromised bike, thus, 
        //     of one bike we calc the prob of getting two bikes (similar for all combinations)
        if (predictionoffset<=arrivaloffset){
            
            //first analyse the probs if no take/return takes place or the take/return occures after currenttime+predictionoffset
            //(probability distribution  obtained from currenttime up to currenttime+totime) 
            IntTuple avCB=getAvailableCapandBikes( s,   predictionoffset);
            int initialbikes=Math.max(Math.min(avCB.avbikes,avCB.avcap) , 0);
            StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,initialbikes);     
            int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB.minpostchanges;
            int requiredslots=1+ additionalResourcesDesiredInProbability +avCB.maxpostchanges;  
            //probabilities if no take/return occurres
            pd.probabilityTake= Math.pow(pc.kOrMoreBikesProbability(requiredbikes), probabilityExponent); 
            pd.probabilityReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots), probabilityExponent);   
  
            //this part is assuming that we consider that a take/return takes place just at currenttime+predictionoffset
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  /*          pd.probabilityTakeAfterTake = Math.pow(pc.kOrMoreBikesProbability(requiredbikes+1), probabilityExponent);
              pd.probabilityReturnAfterTake =pd.probabilityReturn;
              pd.probabilityReturnAfterReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots+1), probabilityExponent);
              pd.probabilityTakeAfterRerturn = pd.probabilityTake;  
  */
            //in this part we assume that the take/return takes place at the beginning
            // adding/resting a bike to the initial bikes
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 
            // assume there was 1 more take (1 bike less)
            initialbikes=Math.max(Math.min(avCB.avbikes-1,avCB.avcap) , 0);
            pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,initialbikes);     
            pd.probabilityTakeAfterTake= Math.pow(pc.kOrMoreBikesProbability(requiredbikes), probabilityExponent); 
            pd.probabilityReturnAfterTake = Math.pow(pc.kOrMoreSlotsProbability(requiredslots), probabilityExponent);   
            // assume there was 1 more return (1 bike more)
            initialbikes=Math.max(Math.min(avCB.avbikes+1,avCB.avcap) , 0);
            pc=new StationProbabilitiesQueueBased(
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                avCB.takedemandrate,avCB.avcap,initialbikes);     
            pd.probabilityTakeAfterRerturn= Math.pow(pc.kOrMoreBikesProbability(requiredbikes), probabilityExponent); 
            pd.probabilityReturnAfterReturn = Math.pow(pc.kOrMoreSlotsProbability(requiredslots), probabilityExponent);   

        }
        //case2: predictionoffset>arrivaloffset
        //     here we calculate the distribution at currenttime+arrivaloffset; then add/return a bike
        //     and calculate the distribution from there to currenttime+predictionoffset 
        else
        {
            //calculate the probability distribution  obtained from currenttime up to currenttime+arrivaloffset 
            IntTuple avCB=getAvailableCapandBikes( s,   arrivaloffset);
            int initialbikes=avCB.avbikes;
            initialbikes=Math.max(Math.min(initialbikes,avCB.avcap) , 0);
            StationProbabilitiesQueueBased pc=new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB.returndemandrate,
                    avCB.takedemandrate,avCB.avcap,initialbikes); 
         
            //analyse the case when no take or return takes place
            // and calculate the prob at currenttime+predictionoffset 
            IntTuple avCB2=getChangesInIntervall( s,   arrivaloffset, predictionoffset);
            int changes=avCB2.changesInTime;
            StationProbabilitiesQueueBased pcnew=new StationProbabilitiesQueueBased(
                pc.getProbabilityDistribution(),changes,
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB2.returndemandrate,
                avCB2.takedemandrate);        
            int requiredbikes=1+ additionalResourcesDesiredInProbability -avCB2.minpostchanges;
            int requiredslots=1+ additionalResourcesDesiredInProbability +avCB2.maxpostchanges;               
            pd.probabilityTake = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
            pd.probabilityReturn =Math.pow( pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);
           
            //analyse the case with a take at currenttime+arrivaloffset and calculate the prob at 
            // currenttime+predictionoffset 
            changes=avCB2.changesInTime-1;
            pcnew=new StationProbabilitiesQueueBased(
                pc.getProbabilityDistribution(),changes,
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB2.returndemandrate,
                avCB2.takedemandrate);        
            pd.probabilityTakeAfterTake = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent);
            pd.probabilityReturnAfterTake =Math.pow(pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);

            //analyse the case with a return at currenttime+arrivaloffset and calculate the prob at 
            // currenttime+predictionoffset 
            changes=avCB2.changesInTime+1;
            pcnew=new StationProbabilitiesQueueBased(
                pc.getProbabilityDistribution(),changes,
                StationProbabilitiesQueueBased.Type.RungeKutta,h,avCB2.returndemandrate,
                avCB2.takedemandrate); 
            pd.probabilityReturnAfterReturn = Math.pow(pcnew.kOrMoreSlotsProbability(requiredslots), probabilityExponent);
            pd.probabilityTakeAfterRerturn = Math.pow(pcnew.kOrMoreBikesProbability(requiredbikes), probabilityExponent); 
        }
        return pd;
    }
    
    // this case is the same as the one before, but the arrival is exactly the same as the predictionoffset
    // that is, we predict at currenttime+predictionoffset with and without 1 more bike taken or returned
    public ProbabilityData calculateFutureProbabilitiesWithAndWithoutArrival(Station sd, double predictionoffset) {
        return calculateFutureProbabilitiesWithAndWithoutArrival(sd, predictionoffset,predictionoffset);
    }

    //methods for calculation probabilities    
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
