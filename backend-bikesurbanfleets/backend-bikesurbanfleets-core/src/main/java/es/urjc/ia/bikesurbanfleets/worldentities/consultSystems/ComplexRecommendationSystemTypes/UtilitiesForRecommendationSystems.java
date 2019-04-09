/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.demand.DemandManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes.PastRecommendations.ExpBikeChangeResult;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author holger
 */
public class UtilitiesForRecommendationSystems {

    RecommendationSystem rs;
    DemandManager dm;

    public UtilitiesForRecommendationSystems(RecommendationSystem rs) {
        this.rs = rs;
        this.dm = rs.getDemandManager();
    }

    // the method returns the difference of the OpenSquaredUtility after taking or returning a bike wrt the situation before
    public double calculateOpenSquaredStationUtilityDifference(StationUtilityData sd, boolean rentbike) {
        Station s =sd.getStation();
        double idealbikes = getCurrentBikeDemand(s);
        double maxidealbikes = sd.getCapacity() - getCurrentSlotDemand(s);
        double currentutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes(), idealbikes, maxidealbikes);
        double newutility;
        if (rentbike) {
            newutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes()-1, idealbikes, maxidealbikes);
        } else {//return bike 
            newutility = getOpenSquaredUtility(s.getCapacity(), s.availableBikes()+1, idealbikes, maxidealbikes);
        }
        return (newutility - currentutility);
    }
    //calculates the utility
    //station utility here is defined as a open function which is 1 is the av bikes is between the 
    //the demand of bikes for the following hour and below the demand of slots for the following hour
    //closed to the boundaries the utility changes squared
    private double getOpenSquaredUtility(int capacity, int avbikes, double minidealbikes, double maxidealbikes) {
        if (minidealbikes <= maxidealbikes) {
            if (avbikes <= minidealbikes) {
                return 1 - Math.pow(((avbikes - minidealbikes) / minidealbikes), 2);
            } else if (avbikes >= maxidealbikes) {
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
    public double calculateClosedSquaredStationUtilityDifference(StationUtilityData sd, boolean rentbike) {
        Station s =sd.getStation();
        double idealbikes = getCurrentBikeDemand(s);
        double maxidealbikes = sd.getCapacity() - getCurrentSlotDemand(s);
        double currentutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes(), idealbikes, maxidealbikes);
        double newutility;
        if (rentbike) {
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()-1, idealbikes, maxidealbikes);
        } else {//return bike 
            newutility = getClosedSquaredUtility(s.getCapacity(), s.availableBikes()+1, idealbikes, maxidealbikes);
        }
        return (newutility - currentutility);
    }

    //calculates the closed utility
    //station utility here is defined as a closed function which is 1 just in the middle between the slot demand and the bike demand
    //closed to the boundaries the utility changes squared
    private double getClosedSquaredUtility(int capacity, int avbikes, double minidealbikes, double maxidealbikes) {
        double bestocupation = (minidealbikes + maxidealbikes) / 2D;
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

    //methods for cost calculations
    
    //DO NOT CHANGE IT IS WORKING :)
    final double minimumMarginProbability=0.001;
    final double unsuccessCostRent=0; 
    final double penalisationfactorrent=0;
    final double unsuccessCostReturn=0; 
    final double penalisationfactorreturn=0;
    private double calculateWayCostRentHeuristic(List<StationUtilityData> way, StationUtilityData sd, double sdprob,
            double margprob, double currentdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        way.add(sd);
        double thiscost = (margprob - minimumMarginProbability) * currentdist;
        double newmargprob = margprob * (1 - sdprob);
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= minimumMarginProbability) {
            return thiscost;
        }
        //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), newmargprob, lookedlist, allstats);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
        if (start) {
            sd.bestNeighbour = closestneighbour;
        }
        double margcost = newmargprob * unsuccessCostRent
                + calculateWayCostRentHeuristic(way, closestneighbour, closestneighbour.getProbabilityTake(), newmargprob, newdist, lookedlist, allstats, false);
        return thiscost + penalisationfactorrent * margcost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateWayCostReturnHeuristic(List<StationUtilityData> way, StationUtilityData sd, double sdprob,
            double margprob, double currentdist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        way.add(sd);
        double thisbikecost = (margprob - minimumMarginProbability) * currentdist;
        double thiswalkdist = sd.getStation().getPosition().distanceTo(destination);
        double newmargprob = margprob * (1 - sdprob);
        double thistotalcost = thisbikecost;
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= minimumMarginProbability) {
            thistotalcost = thistotalcost + thiswalkdist * (margprob - minimumMarginProbability);
            return thistotalcost;
        } else {
            thistotalcost = thistotalcost + thiswalkdist * margprob * sdprob;
        }
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), newmargprob, lookedlist, allstats, destination);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) * this.parameters.bikefactor;
        if (start) {
            sd.bestNeighbour=closestneighbour;
        }
        double margvalue = newmargprob * unsuccessCostReturn
                + calculateWayCostReturnHeuristic(way, closestneighbour, closestneighbour.getProbabilityReturn(), newmargprob, newdist, destination, lookedlist, allstats, false);
        return thistotalcost + penalisationfactorreturn * margvalue;
    }

    private StationUtilityData bestNeighbourRent(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityTake() > this.parameters.minProbRecommendation) {
                double newdist = s.getPosition().distanceTo(nei.getStation().getPosition());
                double altthiscost = (newmargprob - minimumMarginProbability) * newdist;
                double altnewmargprob = newmargprob * (1 - nei.getProbabilityTake());
                if (altnewmargprob <= minimumMarginProbability) {
                    altthiscost = altthiscost;
                } else {
                            altthiscost = altthiscost + (altnewmargprob - minimumMarginProbability) * this.parameters.MaxCostValue;
                }
                if (altthiscost < newbestValueFound) {
                    newbestValueFound = altthiscost;
                    bestneighbour = nei;
                }
            }
        }
        return bestneighbour;
    }

    private StationUtilityData bestNeighbourReturn(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats, GeoPoint destination) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityReturn() > this.parameters.minProbRecommendation) {
                double altthisbikedist = s.getPosition().distanceTo(nei.getStation().getPosition()) * this.parameters.bikefactor;
                double altthisbikecost = (newmargprob - minimumMarginProbability) * altthisbikedist;
                double altthiswalkdist = nei.getStation().getPosition().distanceTo(destination);
                double altnewmargprob = newmargprob * (1 - nei.getProbabilityReturn());
                double alttotalcost = altthisbikecost;
                if (altnewmargprob <= minimumMarginProbability) {
                    alttotalcost = alttotalcost + altthiswalkdist * (newmargprob - minimumMarginProbability);
                } else {
                    alttotalcost = alttotalcost + altthiswalkdist * newmargprob * nei.getProbabilityReturn() +
                            (altnewmargprob - minimumMarginProbability) * this.parameters.MaxCostValue;
                }
                if (alttotalcost < newbestValueFound) {
                    newbestValueFound = alttotalcost;
                    bestneighbour = nei;
                }
            }
        }
        return bestneighbour;
    }

}
