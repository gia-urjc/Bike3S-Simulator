/*
 same as 2,but renting implemented differently
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author holger
 */
public class ComplexCostCalculator {

    //methods for cost calculations
    public ComplexCostCalculator(double marginprob, double maxcost, double unsuccostrent, double unsuccostret,
            double penalfactorrent, double penalfactorret, double walkvel, double cycvel, double minsecondaryprob,
            double maxDistanceRecomendation, UtilitiesProbabilityCalculation probutils,
            boolean squaredTimes, int PredictionNorm) {
        minimumMarginProbability = marginprob;
        unsuccessCostRent = unsuccostrent;
        penalisationfactorrent = penalfactorrent;
        unsuccessCostReturn = unsuccostret;
        penalisationfactorreturn = penalfactorret;
        walkingVelocity=walkvel;
        cyclingVelocity=cycvel;
        minProbSecondaryRecommendation=minsecondaryprob;
        this.maxDistanceRecomendation=maxDistanceRecomendation;
        maxWalktime=this.maxDistanceRecomendation/walkingVelocity;
        this.probutils=probutils;
        maxCostValue=maxcost;
        useSuaredTimes=squaredTimes;
        predictionNormalisation=PredictionNorm;
    }

    final boolean useSuaredTimes;
    final int predictionNormalisation;
    final double minimumMarginProbability;
    final double unsuccessCostRent;
    final double penalisationfactorrent;
    final double unsuccessCostReturn;
    final double penalisationfactorreturn;
    final double walkingVelocity;
    final double cyclingVelocity;
    final double minProbSecondaryRecommendation;
    final double maxCostValue;
    final double maxDistanceRecomendation;
    final double maxWalktime;
    UtilitiesProbabilityCalculation probutils;


    private double getSqarewalkTimeRent(double accwalktime) {
        if (useSuaredTimes)
        return (accwalktime*accwalktime)/maxWalktime;
        else return accwalktime;
    }
    private double getSqareReturnDistanceCost(double accbiketime, double walktime) {
        if (useSuaredTimes)
            return (accbiketime + walktime*walktime)/maxWalktime;
        else return accbiketime+walktime;
    }

    private double calculateWayCostRentHeuristic(List<StationUtilityData> way, StationUtilityData sd, double takeprob,
            double margprob, double walktime,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start,double accwalktime) {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        way.add(sd);
        double thisprob=margprob * takeprob;
        double newmargprob = margprob -thisprob;
        double newaccwalktime=accwalktime+walktime;
        double sqwalktime=getSqarewalkTimeRent(newaccwalktime);
        if (newmargprob <= minimumMarginProbability) {
            return (margprob-minimumMarginProbability)*sqwalktime;
        }
        double extrastationpenalizationcost=(newmargprob-minimumMarginProbability) * unsuccessCostRent;
        double thiscost= thisprob * sqwalktime;
       //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), newmargprob, lookedlist, allstats,newaccwalktime);
        double margcost;
        if (closestneighbour!=null) {
            double newtime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/ walkingVelocity;
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double neighbourrentprob=probutils.calculateTakeProbability(closestneighbour.getStation(), newaccwalktime+newtime);
            margcost = calculateWayCostRentHeuristic(way, closestneighbour, neighbourrentprob, newmargprob, newtime, lookedlist, allstats, false,newaccwalktime);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at maxCostValue seconds
            margcost = (newmargprob - minimumMarginProbability) * getSqarewalkTimeRent(newaccwalktime+maxCostValue);
        }
        return thiscost + extrastationpenalizationcost + penalisationfactorrent * margcost;
    }

    public double calculateWayCostRentHeuristic(List<StationUtilityData> way, StationUtilityData sd,
            double margprob, double walktime,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        return calculateWayCostRentHeuristic(way, sd, sd.getProbabilityTake(),
            margprob, walktime, lookedlist, allstats, start,0);
    }
    
    public double calculateCostRentHeuristic(StationUtilityData sd,
            double margprob, double walktime,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        return calculateWayCostRentHeuristic(new ArrayList<>(), sd, sd.getProbabilityTake(),
            margprob, walktime, lookedlist, allstats, start,0);
    }
    
 
    public double calculateCostRentHeuristic(StationUtilityData sd, double takeprob,
            double margprob, double currenttime,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        return calculateWayCostRentHeuristic(new ArrayList<>(), sd, takeprob,
            margprob, currenttime, lookedlist, allstats, start, 0);
    }

    //DO NOT CHANGE IT IS WORKING :)
    public double calculateWayCostReturnHeuristic(List<StationUtilityData> way, StationUtilityData sd, double returnprob,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start,double accbiketime) {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        way.add(sd);
        double thisprob=margprob * returnprob;
        double newmargprob = margprob -thisprob;
        double newaccbiketime=accbiketime+biketime;
        double walktime = sd.getStation().getPosition().distanceTo(destination)/ walkingVelocity;
        double timecost=getSqareReturnDistanceCost(newaccbiketime, walktime);
        if (newmargprob <= minimumMarginProbability) {
            return (margprob-minimumMarginProbability)*(timecost);
        }
        double thiscost= thisprob * (timecost);
        double extrastationpenalizationcost=(newmargprob-minimumMarginProbability) * unsuccessCostReturn;
        // find best neigbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), newmargprob, lookedlist, allstats, destination, newaccbiketime);
        double margcost;
        if (closestneighbour!=null) {
            double newbiketime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) / cyclingVelocity;
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double neighbourreturnprob=probutils.calculateReturnProbability(closestneighbour.getStation(), newaccbiketime+newbiketime);
            margcost = calculateWayCostReturnHeuristic(way, closestneighbour, neighbourreturnprob, newmargprob, newbiketime, destination, lookedlist, allstats, false, newaccbiketime);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at maxCostValue seconds
            margcost = (newmargprob - minimumMarginProbability) * getSqareReturnDistanceCost(newaccbiketime,maxCostValue);
        }
        return thiscost + extrastationpenalizationcost + penalisationfactorreturn * margcost;
    }
 
    public double calculateWayCostReturnHeuristic(List<StationUtilityData> way, StationUtilityData sd,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
    return calculateWayCostReturnHeuristic(way, sd, sd.getProbabilityReturn(),
            margprob, biketime, destination,lookedlist,allstats,  start,0 );
    }

       //DO NOT CHANGE IT IS WORKING :)
    public double calculateCostReturnHeuristic(StationUtilityData sd,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
    return calculateWayCostReturnHeuristic(new ArrayList<>(), sd, sd.getProbabilityReturn(),
            margprob, biketime, destination,lookedlist,allstats,  start,0);
    }
    
    public double calculateCostReturnHeuristic(StationUtilityData sd, double returnprob,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
    return calculateWayCostReturnHeuristic(new ArrayList<>(), sd, returnprob,
            margprob, biketime, destination,lookedlist,allstats,  start,0);
    }

 /* with probs at the arrivaltime at the first station
private StationUtilityData bestNeighbourRent(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalktime) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityTake() > minProbSecondaryRecommendation) {
                double newacctime=accwalktime+s.getPosition().distanceTo(nei.getStation().getPosition())/ walkingVelocity ;
                double timecost=getSqarewalkTimeRent(newacctime);
                double thisprob=newmargprob * nei.getProbabilityTake();
                double altnewmargprob = newmargprob -thisprob;
                //calculate the cost of this potential neighbour
                double altthiscost = thisprob * timecost + altnewmargprob  * getSqarewalkTimeRent(newacctime+maxCostValue);
                if (altthiscost < newbestValueFound) {
                    newbestValueFound = altthiscost;
                    bestneighbour = nei;
                }
            }
        }
        return bestneighbour;
    }
*/
    //with probabilities recalculated at the correct time
    private StationUtilityData bestNeighbourRent(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalktime) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double newacctime=accwalktime+s.getPosition().distanceTo(nei.getStation().getPosition())/ walkingVelocity ;
          //holger      if (newacctime<= (maxWalktime*1.2)) {
                    double rentprob=probutils.calculateTakeProbability(nei.getStation(), newacctime);
                    if (rentprob > minProbSecondaryRecommendation) {
                        double timecost=getSqarewalkTimeRent(newacctime);
                        double thisprob=newmargprob * rentprob;
                        double altnewmargprob = newmargprob -thisprob;
                        //calculate the cost of this potential neighbour
                        double altthiscost = thisprob * timecost + altnewmargprob  * getSqarewalkTimeRent(newacctime+maxCostValue);
                        if (altthiscost < newbestValueFound) {
                            newbestValueFound = altthiscost;
                            bestneighbour = nei;
                        }
                    }
        //        }
            }
        }
        return bestneighbour;
    }

    private StationUtilityData bestNeighbourReturn(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats, GeoPoint destination,
            double accbiketime) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double altthisbiketime = accbiketime + s.getPosition().distanceTo(nei.getStation().getPosition()) / cyclingVelocity;
                double returnprob=probutils.calculateReturnProbability(nei.getStation(), altthisbiketime);
                if (returnprob > minProbSecondaryRecommendation) {
                    double altthiswalktime = nei.getStation().getPosition().distanceTo(destination) / walkingVelocity;
                    double timecost=getSqareReturnDistanceCost(altthisbiketime, altthiswalktime);
                    double thisprob=newmargprob * returnprob;
                    double altnewmargprob = newmargprob -thisprob;
                    //calculate the cost of this potential neighbour
                    double  altthiscost = thisprob * timecost +
                                + altnewmargprob * getSqareReturnDistanceCost(altthisbiketime,maxCostValue);
                    if (altthiscost < newbestValueFound) {
                        newbestValueFound = altthiscost;
                        bestneighbour = nei;
                    }
                }
            }
        }
        return bestneighbour;
    }
    
    
    //global cost calculation. calculates the cost of taking/returning and also the cost differences
    // returns the global costs
    public double calculateCostsRentAtStation(StationUtilityData sd,
            List<StationUtilityData> allstats) {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<StationUtilityData> way = new LinkedList<StationUtilityData>();
        double usercosttake = calculateWayCostRentHeuristic(way, sd , 1, sd.getWalkTime(), lookedlist, allstats, true);

        //analyze global costs
        double margprob = 1;
        double acctakecost = 0;
        double accreturncost = 0;
        int timeoffset = (int)sd.getWalkTime();
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        for (StationUtilityData wp : way) {
            if (margprob <= minimumMarginProbability) {
                throw new RuntimeException("error parameters");
            }
            //calculate takecost difference
            newlookedlist=new ArrayList<>(lookedlist);
            double costtake = calculateCostRentHeuristic(wp, 1, 0, newlookedlist, allstats, false);
            newlookedlist=new ArrayList<>(lookedlist);
            double costtakeafter = calculateCostRentHeuristic(wp, wp.getProbabilityTakeAfterTake(), 1, 0, newlookedlist, allstats, false);
            double extracosttake=(costtakeafter - costtake) ;

            //calculate return cost difference
            GeoPoint hipodestination = wp.getStation().getPosition();
            newlookedlist=new ArrayList<>(lookedlist);
            double costreturnhip = calculateCostReturnHeuristic(wp, 1, 0, hipodestination, newlookedlist, allstats, false);
            newlookedlist=new ArrayList<>(lookedlist);
            double costreturnafterhip = calculateCostReturnHeuristic(wp, wp.getProbabilityReturnAfterTake(), 1, 0, hipodestination, newlookedlist, allstats, false);
            double extracostreturn=(costreturnafterhip - costreturnhip) ;

            if (extracostreturn>0 || extracosttake<0){
                    System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.getStation(), timeoffset);
            extracostreturn = extracostreturn* getReturnFactor(wp.getStation(), timeoffset);;

            double takeprob=margprob*wp.getProbabilityTake(); //prob with witch the user would take a bike at the station
            double newmargprob = margprob * (1 - wp.getProbabilityTake());

            if (newmargprob <= minimumMarginProbability) {
                acctakecost+= (margprob - minimumMarginProbability)*extracosttake;
                accreturncost+= (margprob - minimumMarginProbability)*extracostreturn;
            }
            else {
                acctakecost+= takeprob * extracosttake;
                accreturncost+= takeprob * extracostreturn;
                margprob=newmargprob;
            }
            lookedlist.add(wp);
        }

        double globalcost = usercosttake + acctakecost + accreturncost;
        sd.setIndividualCost(usercosttake).setTakecostdiff(acctakecost).setReturncostdiff(accreturncost);
        return globalcost;

    }

    public double calculateCostsReturnAtStation(StationUtilityData sd, GeoPoint destination,
            List<StationUtilityData> allstats) {
        //return costs
        //take a close point to the station as hipotetical detsination
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<StationUtilityData> way = new LinkedList<StationUtilityData>();
        double usercostreturn = calculateWayCostReturnHeuristic(way, sd, 1, sd.getBiketime(), destination, lookedlist, allstats, true);

        //analyze global costs
        double margprob = 1;
        double acctakecost = 0;
        double accreturncost = 0;
        int timeoffset = (int) (sd.getBiketime());
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        for (StationUtilityData wp : way) {
            if (margprob <= minimumMarginProbability) {
                throw new RuntimeException("error parameters");
            }
            //calculate takecost difference
            newlookedlist=new ArrayList<>(lookedlist);
            double costtake = calculateCostRentHeuristic(wp, 1, 0, newlookedlist, allstats, false);
            newlookedlist=new ArrayList<>(lookedlist);
            double costtakeafter = calculateCostRentHeuristic(wp, wp.getProbabilityTakeAfterRerturn(), 1, 0, newlookedlist, allstats, false);
            double extracosttake=(costtakeafter - costtake) ;
            //calculate return cost difference
            GeoPoint hipodestination = wp.getStation().getPosition();
            newlookedlist=new ArrayList<>(lookedlist);
            double costreturnhip = calculateCostReturnHeuristic(wp, 1, 0, hipodestination, newlookedlist, allstats, false);
            newlookedlist=new ArrayList<>(lookedlist);
            double costreturnafterhip = calculateCostReturnHeuristic(wp, wp.getProbabilityReturnAfterReturn(), 1, 0, hipodestination, newlookedlist, allstats, false);
            double extracostreturn=(costreturnafterhip - costreturnhip) ;

            if (extracostreturn<0 || extracosttake>0){
                    System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.getStation(), timeoffset);
            extracostreturn = extracostreturn* getReturnFactor(wp.getStation(), timeoffset);;

            double retprob=margprob*wp.getProbabilityReturn(); //prob with witch the user would take a bike at the station
            double newmargprob = margprob * (1 - wp.getProbabilityReturn());

            if (newmargprob <= minimumMarginProbability) {
                acctakecost+= (margprob - minimumMarginProbability)*extracosttake;
                accreturncost+= (margprob - minimumMarginProbability)*extracostreturn;
            }
            else {
                acctakecost+= retprob * extracosttake;
                accreturncost+= retprob * extracostreturn;
                margprob=newmargprob;
            }
            lookedlist.add(wp);
        }

        double globalcost = usercostreturn + acctakecost + accreturncost;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(acctakecost).setReturncostdiff(accreturncost);
        return globalcost;
    }
    private double getTakeFactor(Station s, double timeoffset){
         switch(predictionNormalisation){
            case (0) :
                return 1;
            case (1) :
                return probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset);
            case (3) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s,timeoffset);
        }
         return 1;
    }
     private double getReturnFactor(Station s, double timeoffset){
        switch(predictionNormalisation){
            case (0) :
                 return 1;
            case (1) :
                return probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset);
            case (3) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,timeoffset);
        }
         return 1;
    }

}
