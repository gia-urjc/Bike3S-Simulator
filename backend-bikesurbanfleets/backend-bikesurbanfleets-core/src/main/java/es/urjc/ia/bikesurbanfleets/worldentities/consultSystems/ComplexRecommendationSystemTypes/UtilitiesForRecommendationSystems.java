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

    RecommendationSystem rs;
    DemandManager dm;
    public UtilitiesForRecommendationSystems(RecommendationSystem rs) {
        this.rs=rs;
        this.dm=rs.getDemandManager();
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
        k=k-1;
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
        return dm.getReturnDemandGlobal( current);
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