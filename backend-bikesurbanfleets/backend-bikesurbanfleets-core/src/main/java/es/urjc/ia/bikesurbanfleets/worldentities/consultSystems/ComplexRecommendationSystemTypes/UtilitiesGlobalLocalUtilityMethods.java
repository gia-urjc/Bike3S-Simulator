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
public class UtilitiesGlobalLocalUtilityMethods {

    DemandManager dm;

    public UtilitiesGlobalLocalUtilityMethods(DemandManager dm) {
        this.dm = dm;
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

}
