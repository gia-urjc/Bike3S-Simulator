/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes.PastRecommendations.ExpBikeChangeResult;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.time.LocalDateTime;

/**
 *
 * @author holger
 */
public class UtilitiesForRecommendationSystems {

    DemandManager dm;

    public UtilitiesForRecommendationSystems(RecommendationSystem rm) {
        this.dm = rm.getDemandManager();
    }

    // the method returns the difference of the OpenSquaredUtility after taking or returning a bike wrt the situation before
    public double calculateOpenSquaredStationUtilityDifference(StationUtilityData sd, boolean rentbike) {
        Station s =sd.getStation();
        double bikedemand = dm.getStationTakeRatePerHour(s.getId(),SimulationDateTime.getCurrentSimulationDateTime() );
        double slotdemand = dm.getStationReturnRatePerHour(s.getId(),SimulationDateTime.getCurrentSimulationDateTime());
        double currentutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes(), bikedemand, slotdemand);
        double newutility;
        if (rentbike) {
            newutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes()-1, bikedemand, slotdemand);
        } else {//return bike 
            newutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes()+1, bikedemand, slotdemand);
        }
        return (newutility - currentutility);
    }
    //calculates the utility
    //station utility here is defined as a open function which is 1 is the av bikes is between the 
    //the demand of bikes for the following hour and below the demand of slots for the following hour
    //closed to the boundaries the utility changes squared
    static protected double getOpenSquaredUtility(double capacity, double avbikes, double bikedemand, double slotdemand) {
        double minidealbikes=bikedemand;
        if (minidealbikes<1) minidealbikes=1;
        double maxidealbikes=capacity-slotdemand;
        if (maxidealbikes > capacity-1) maxidealbikes = capacity-1;
        if (minidealbikes <= maxidealbikes) {
            if (avbikes < minidealbikes) {
                return 1 - Math.pow(((avbikes - minidealbikes) / minidealbikes), 2);
            } else if (avbikes > maxidealbikes) {
                return 1 - Math.pow(((avbikes - maxidealbikes) / (capacity - maxidealbikes)), 2);
            } else {//if ocupation is just between max and min
                return 1;
            }
        } else { //idealbikes > max idealbikes
            double bestocupation = (minidealbikes + maxidealbikes) / 2D;
            //          double bestocupation = (idealbikes * cap)/(cap - maxidealbikes  ) ;
            if (avbikes <= bestocupation) {
                return 1 - Math.pow(((avbikes - bestocupation) / bestocupation), 2);
            } else {
                double aux = capacity - bestocupation;
                return 1 - Math.pow(((avbikes - bestocupation) / aux), 2);
            }
        }
    }

    // the method returns the difference of the ClosedSquaredUtility after taking or returning a bike wrt the situation before
    public double calculateClosedSquaredStationUtilityDifferencewithDemand(Station s, boolean rentbike) {
        double bikedemand = dm.getStationTakeRatePerHour(s.getId(),SimulationDateTime.getCurrentSimulationDateTime() );
        double slotdemand = dm.getStationReturnRatePerHour(s.getId(),SimulationDateTime.getCurrentSimulationDateTime());
        double bestocupation = (bikedemand + s.getCapacity() -slotdemand) / 2D;
        if (bestocupation<1) bestocupation=1;
        if (bestocupation>s.getCapacity()-1) bestocupation=s.getCapacity()-1;
        double currentutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes(), bestocupation);
        double newutility;
        if (rentbike) {
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()-1, bestocupation);
        } else {//return bike 
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()+1, bestocupation);
        }
        return (newutility - currentutility);
    }

    // the method returns the difference of the ClosedSquaredUtility after taking or returning a bike wrt the situation before
    public static double calculateClosedSquaredStationUtilityDifferencewithoutDemand(Station s, boolean rentbike) {
        double bestocupation = (s.getCapacity()) / 2D;
        double currentutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes(), bestocupation);
        double newutility;
        if (rentbike) {
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()-1, bestocupation);
        } else {//return bike 
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()+1, bestocupation);
        }
        return (newutility - currentutility);
    }

    //calculates the closed utility
    //station utility here is defined as a closed function which is 1 just in the middle between the slot demand and the bike demand
    //closed to the boundaries the utility changes squared
    static private double getClosedSquaredUtility(int capacity, int avbikes, double bestocupation) {
        if (avbikes <= bestocupation) {
            return 1 - Math.pow(((avbikes - bestocupation) / bestocupation), 2);
        } else {
            double aux = capacity - bestocupation;
            return 1 - Math.pow(((avbikes - bestocupation) / aux), 2);
        }
    }

    //methods for calculation probabilities    
    public void calculateProbabilities(StationUtilityData sd, double timeoffset,
            boolean takeintoaccountexpected, boolean takeintoaccountcompromised,
            PastRecommendations pastrecs, double POBABILITY_USERSOBEY
    ) {
        Station s = sd.getStation();
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            ExpBikeChangeResult er = pastrecs.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * POBABILITY_USERSOBEY);
            estimatedslots -= (int) Math.floor(er.changes * POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * POBABILITY_USERSOBEY);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * POBABILITY_USERSOBEY);
                //            }
            }
        }
        double takedemandrate = dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double returndemandrate = dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double probbike = SellamDistribution.calculateCDFSkellamProbability(returndemandrate, takedemandrate, k);
        double probbikeaftertake = probbike - SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);
        k = k - 1;
        double probbikeafterreturn = probbike + SellamDistribution.calculateSkellamProbability(returndemandrate, takedemandrate, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        double probslot = SellamDistribution.calculateCDFSkellamProbability(takedemandrate, returndemandrate, k);
        double probslotafterreturn = probslot - SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);
        k = k - 1;
        double probslotaftertake = probslot + SellamDistribution.calculateSkellamProbability(takedemandrate, returndemandrate, k);

        sd.setProbabilityTake(probbike)
                .setProbabilityTakeAfterTake(probbikeaftertake)
                .setProbabilityTakeAfterRerturn(probbikeafterreturn)
                .setProbabilityReturn(probslot)
                .setProbabilityReturnAfterTake(probslotaftertake)
                .setProbabilityReturnAfterReturn(probslotafterreturn);
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
