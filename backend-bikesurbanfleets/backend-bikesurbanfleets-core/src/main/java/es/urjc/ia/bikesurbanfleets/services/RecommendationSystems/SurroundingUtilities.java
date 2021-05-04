/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author holger
 */
public class SurroundingUtilities {
    
      public static double getSurroundingBikeDemand(DemandManager dm, Station candidatestation, List<Station> otherstations,double maxdistancesurrounding ) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (maxdistancesurrounding - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / maxdistancesurrounding;
            multiplication = dm.getStationTakeRatePerHour(other.getId(), current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    public static double getSurroundingSlotDemand(DemandManager dm,Station candidatestation, List<Station> otherstations, double maxdistancesurrounding) {
        double accideal = 0;
        double factor, multiplication;
        LocalDateTime current = SimulationDateTime.getCurrentSimulationDateTime();
        for (Station other : otherstations) {
            factor = (maxdistancesurrounding - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / maxdistancesurrounding;
            multiplication = dm.getStationReturnRatePerHour(other.getId(), current) * factor;
            accideal += multiplication;
        }
        return accideal;
    }

    public static double getSurroundingOcupation(Station candidatestation, List<Station> otherstations,double maxdistancesurrounding) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (maxdistancesurrounding - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / maxdistancesurrounding;
            multiplication = other.availableBikes() * factor;
            accocc += multiplication;
        }
        return accocc;
    }

    public static double getSurroundingCapacity(Station candidatestation, List<Station> otherstations,double maxdistancesurrounding) {
        double accocc = 0;
        double factor, multiplication;
        for (Station other : otherstations) {
            factor = (maxdistancesurrounding - candidatestation.getPosition().eucleadeanDistanceTo(other.getPosition())) / maxdistancesurrounding;
            multiplication = other.getCapacity() * factor;
            accocc += multiplication;
        }
        return accocc;
    }
  
}
