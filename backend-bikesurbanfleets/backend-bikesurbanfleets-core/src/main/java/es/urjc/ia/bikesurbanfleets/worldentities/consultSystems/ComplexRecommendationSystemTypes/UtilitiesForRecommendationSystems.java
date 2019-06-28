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
        double bikedemand = getCurrentBikeDemand(s);
        double slotdemand = getCurrentSlotDemand(s);
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
        double bestocupation = (getCurrentBikeDemand(s) + s.getCapacity() - getCurrentSlotDemand(s)) / 2D;
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
        double takedemandattimeoffset = (getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (getCurrentSlotDemand(s) * timeoffset) / 3600D;

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double probbike = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        double probbikeaftertake = probbike - SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        k = k - 1;
        double probbikeafterreturn = probbike + SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        double probslot = SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);
        double probslotafterreturn = probslot - SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);
        k = k - 1;
        double probslotaftertake = probslot + SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        sd.setProbabilityTake(probbike)
                .setProbabilityTakeAfterTake(probbikeaftertake)
                .setProbabilityTakeAfterRerturn(probbikeafterreturn)
                .setProbabilityReturn(probslot)
                .setProbabilityReturnAfterTake(probslotaftertake)
                .setProbabilityReturnAfterReturn(probslotafterreturn);
    }
    //methods for calculation probabilities    
    public double calculateProbabilityAtLeast1UserArrivingForTake(Station s, double timeoffset) {
        double takedemandattimeoffset = (getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (getCurrentSlotDemand(s) * timeoffset) / 3600D;
        return SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, 1);
    }
    public double calculateProbabilityAtLeast1UserArrivingForReturn(Station s, double timeoffset) {
        double takedemandattimeoffset = (getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (getCurrentSlotDemand(s) * timeoffset) / 3600D;
        return SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, 1);
    }

    
    public double getGlobalProbabilityImprovementIfTake(StationUtilityData sd ) {
        int timeoffset=(int)sd.getWalkTime();
        double futtakedemand = getFutureBikeDemand(sd.getStation(),  timeoffset);
        double futreturndemand = getFutureSlotDemand(sd.getStation(),  timeoffset);
        double futglobaltakedem = getFutureGlobalBikeDemand( timeoffset);
        double futglobalretdem = getFutureGlobalSlotDemand( timeoffset);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.getProbabilityTakeAfterTake()-sd.getProbabilityTake())
                + (futreturndemand / futglobalretdem) * 
                (sd.getProbabilityReturnAfterTake()-sd.getProbabilityReturn());
        return relativeimprovemente;
    }

    public double getGlobalProbabilityImprovementIfReturn(StationUtilityData sd) {
        int timeoffset =(int) sd.getBiketime();
        double futtakedemand = getFutureBikeDemand(sd.getStation(), timeoffset);
        double futreturndemand = getFutureSlotDemand(sd.getStation(),  timeoffset);
        double futglobaltakedem = getFutureGlobalBikeDemand( timeoffset);
        double futglobalretdem = getFutureGlobalSlotDemand( timeoffset);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * 
                (sd.getProbabilityTakeAfterRerturn()-sd.getProbabilityTake())
                + (futreturndemand / futglobalretdem) * 
                (sd.getProbabilityReturnAfterReturn()-sd.getProbabilityReturn());
        return relativeimprovemente;
    }
 
    //methods for acessing demand data
    public double getCurrentSlotDemand(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return dm.getReturnDemandStation(s.getId(), current);
    }

    public double getCurrentBikeDemand(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return dm.getTakeDemandStation(s.getId(), current);
    }

    public double getCurrentGlobalSlotDemand() {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return dm.getReturnDemandGlobal(current);
    }

    public double getCurrentGlobalBikeDemand() {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        return dm.getTakeDemandGlobal(current);
    }

    public double getFutureSlotDemand(Station s, int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return dm.getReturnDemandStation(s.getId(), current);
    }

    public double getFutureBikeDemand(Station s, int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return dm.getTakeDemandStation(s.getId(), current);
    }

    public double getFutureGlobalSlotDemand(int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return dm.getReturnDemandGlobal(current);
    }

    public double getFutureGlobalBikeDemand(int secondsoffset) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime().plusSeconds(secondsoffset);
        return dm.getTakeDemandGlobal(current);
    }

    public double getCurrentFutueScaledSlotDemandNextHour(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        LocalDateTime futuredate = current.plusHours(1);
        double currendem = dm.getReturnDemandStation(s.getId(), current);
        double futuredem = dm.getReturnDemandStation(s.getId(), futuredate);
        double futureprop = ((double) current.getMinute()) / 59D;
        return futuredem * futureprop + (1 - futureprop) * currendem;
    }

    public double getCurrentFutueScaledBikeDemandNextHour(Station s) {
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        LocalDateTime futuredate = current.plusHours(1);
        double currendem = dm.getTakeDemandStation(s.getId(), current);
        double futuredem = dm.getTakeDemandStation(s.getId(), futuredate);
        double futureprop = ((double) current.getMinute()) / 59D;
        return futuredem * futureprop + (1 - futureprop) * currendem;
    }
}
