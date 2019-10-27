/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ProbabilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author holger
 */
public class ComplexCostCalculator3 {
    

    //methods for cost calculations
    public ComplexCostCalculator3(double marginprob, double maxcost, double unsuccostrent, double unsuccostret,
            double penalfactorrent, double penalfactorret, double walkvel, double cycvel, double minsecondaryprob,
            double maxDistanceRecomendation, UtilitiesProbabilityCalculator probutils,
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
    UtilitiesProbabilityCalculator probutils;


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

    //claculates a "good" way and its cost for taking a bike
    //this is a sequence of stantions where the user can take a bike up to a probabilkity of 1-minimumMarginProbability
    //probtimeoffset is a timeoffset for calculating cost in the future
    //e.g. calculate the cost for getting a bike if the user appears at a walkingtime distances in 10 minutes (values are in seconds)
    private double calculateWayCostRentHeuristic(
            List<stationPoint> way, StationUtilityData sd, 
            double margprob, double walktime,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, 
            boolean start,double accwalktime,
            double takeprob, double probtimeoffset) {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        double newaccwalktime=accwalktime+walktime;

        double thisprob=margprob * takeprob;
        double newmargprob = margprob -thisprob;
        double sqwalktime=getSqarewalkTimeRent(newaccwalktime);
        if (newmargprob <= minimumMarginProbability) {
            way.add(new stationPoint(sd,newaccwalktime, margprob-minimumMarginProbability, 0));
            return (margprob-minimumMarginProbability)*sqwalktime;
        }
        way.add(new stationPoint(sd,newaccwalktime, thisprob, 0));
        double extrastationpenalizationcost=(newmargprob-minimumMarginProbability) * unsuccessCostRent;
        double thiscost= thisprob * sqwalktime;
       //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), lookedlist, allstats,newaccwalktime, probtimeoffset);
/*        StationUtilityData closestneighbour2 = bestNeighbourRentBad(sd.getStation(), newmargprob, lookedlist, allstats,newaccwalktime, probtimeoffset);
        if (closestneighbour!=null && closestneighbour2!=null&& closestneighbour.getStation().getId()!=closestneighbour2.getStation().getId()) {
            double newtime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/ walkingVelocity;
            System.out.println("bestneigbour different take good:"+ closestneighbour.getStation().getId() + " "+
                   newtime+ " "+
                   probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset+newaccwalktime+newtime) );
            newtime = sd.getStation().getPosition().distanceTo(closestneighbour2.getStation().getPosition())/ walkingVelocity;
            System.out.println("bad:"+ closestneighbour2.getStation().getId() + " "+
                   newtime+ " "+
                   probutils.calculateTakeProbability(closestneighbour2.getStation(), probtimeoffset+newaccwalktime+newtime) );
        }
*/        double margcost;
        if (closestneighbour!=null) {
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double newtime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/ walkingVelocity;
            double neighbourrentprob=probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset+newaccwalktime+newtime);
            margcost = calculateWayCostRentHeuristic(way, closestneighbour, newmargprob, newtime, lookedlist, allstats, false, newaccwalktime,neighbourrentprob, probtimeoffset);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at maxCostValue seconds
            margcost = (newmargprob - minimumMarginProbability) * getSqarewalkTimeRent(newaccwalktime+maxCostValue);
        }
        return thiscost + extrastationpenalizationcost + penalisationfactorrent * margcost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateWayCostReturnHeuristic(List<stationPoint> way, StationUtilityData sd,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start,double accbiketime,
            double returnprob, double probtimeoffset) {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        double newaccbiketime=accbiketime+biketime;

        double thisprob=margprob * returnprob;
        double newmargprob = margprob -thisprob;
        double walktime = sd.getStation().getPosition().distanceTo(destination)/ walkingVelocity;
        double timecost=getSqareReturnDistanceCost(newaccbiketime, walktime);
        if (newmargprob <= minimumMarginProbability) {
            way.add(new stationPoint(sd,newaccbiketime,0, margprob-minimumMarginProbability));
            return (margprob-minimumMarginProbability)*(timecost);
        }
        way.add(new stationPoint(sd,newaccbiketime,0, thisprob));
        double thiscost= thisprob * (timecost); 
        double extrastationpenalizationcost=(newmargprob-minimumMarginProbability) * unsuccessCostReturn;
        // find best neigbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(),lookedlist, allstats, destination, newaccbiketime, probtimeoffset);
   /*     StationUtilityData closestneighbour2 = bestNeighbourReturnBad(sd.getStation(), newmargprob, lookedlist, allstats,destination, newaccbiketime, probtimeoffset);
        if (closestneighbour!=null && closestneighbour2!=null && closestneighbour.getStation().getId()!=closestneighbour2.getStation().getId()){
            double newbiketime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/ cyclingVelocity;
            double newwalktime = closestneighbour.getStation().getPosition().distanceTo(destination)/ walkingVelocity;
            System.out.println("bestneigbour different return good:"+ closestneighbour.getStation().getId() + " "+
                   newbiketime+ " "+ newwalktime + " " +
                   probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset+newaccbiketime+newbiketime) );
             newwalktime = closestneighbour2.getStation().getPosition().distanceTo(destination)/ walkingVelocity;
            newbiketime = sd.getStation().getPosition().distanceTo(closestneighbour2.getStation().getPosition())/ cyclingVelocity;
            System.out.println("bad:"+ closestneighbour2.getStation().getId() + " "+
                   newbiketime+ " "+ newwalktime + " " +
                   probutils.calculateReturnProbability(closestneighbour2.getStation(), probtimeoffset+newaccbiketime+newbiketime) );
        }
  */      double margcost;
        if (closestneighbour!=null) {
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double newbiketime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) / cyclingVelocity;
            double neighbourreturnprob=probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset+newaccbiketime+newbiketime);
            margcost = calculateWayCostReturnHeuristic(way, closestneighbour, newmargprob, newbiketime, destination, lookedlist, allstats, false, newaccbiketime,neighbourreturnprob,probtimeoffset);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at maxCostValue seconds
            margcost = (newmargprob - minimumMarginProbability) * getSqareReturnDistanceCost(newaccbiketime,maxCostValue);
        }
        return thiscost + extrastationpenalizationcost + penalisationfactorreturn * margcost;
    }
 
    public double calculateCostRentHeuristicNow(StationUtilityData sd, List<StationUtilityData> allstats) {
        return calculateWayCostRentHeuristic(new ArrayList<>(), sd,
            1, sd.getWalkTime(), new ArrayList<>(), allstats, true,0,sd.getProbabilityTake(),0);
    }
    //DO NOT CHANGE IT IS WORKING :)
    public double calculateCostReturnHeuristicNow(StationUtilityData sd,
            GeoPoint destination,            
            List<StationUtilityData> allstats) {
    return calculateWayCostReturnHeuristic(new ArrayList<>(), sd,
            1, sd.getBiketime(), destination,new ArrayList<>(),allstats,  true, 0, sd.getProbabilityReturn(),0);
    }
    
    //with probabilities recalculated at the correct time
    private StationUtilityData bestNeighbourRent(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalktime, double probtimeoffset) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double newacctime=accwalktime+s.getPosition().distanceTo(nei.getStation().getPosition())/ walkingVelocity ;
                if (newacctime<= (maxWalktime*1.2)) {
                    double rentprob=probutils.calculateTakeProbability(nei.getStation(), newacctime+probtimeoffset);
                    if (rentprob > minProbSecondaryRecommendation) {
                        double timecost=getSqarewalkTimeRent(newacctime);
                        double thisprob= rentprob;
                        double altnewmargprob = 1 -thisprob;
                        //calculate the cost of this potential neighbour
                        double altthiscost = thisprob * timecost + altnewmargprob  * getSqarewalkTimeRent(newacctime+maxCostValue);
                        if (altthiscost < newbestValueFound) {
                            newbestValueFound = altthiscost;
                            bestneighbour = nei;
                        }
                    }
                }
            }
        }
        return bestneighbour;
    }

    private StationUtilityData bestNeighbourReturn(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats, GeoPoint destination,
            double accbiketime, double probtimeoffset) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double altthisbiketime = accbiketime + s.getPosition().distanceTo(nei.getStation().getPosition()) / cyclingVelocity;
                double returnprob=probutils.calculateReturnProbability(nei.getStation(), altthisbiketime+ probtimeoffset);
                if (returnprob > minProbSecondaryRecommendation) {
                    double altthiswalktime = nei.getStation().getPosition().distanceTo(destination) / walkingVelocity;
                    double timecost=getSqareReturnDistanceCost(altthisbiketime, altthiswalktime);
                    double thisprob= returnprob;
                    double altnewmargprob = 1 -thisprob;
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
    
    class stationPoint {
        StationUtilityData sd;
        double offsettimereached;
        double takeprob;
        double returnprob;
        stationPoint(StationUtilityData sd, double t, double tprob, double rprob){
            this.sd=sd;
            offsettimereached=t;
            takeprob=tprob;
            returnprob=rprob;
        }
    }
    
    //global cost calculation. calculates the cost of taking/returning and also the cost differences
    // returns the global costs
    public double calculateCostsRentAtStation(StationUtilityData sd,
            List<StationUtilityData> allstats, double timeintervallforPrediction) {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercosttake = calculateWayCostRentHeuristic(way, sd , 1, sd.getWalkTime(), lookedlist, allstats, true, 0, sd.getProbabilityTake(),0);

        //analyze costs earnings in the timeintervall
        // we take the highest value between timeintervall and the potential arrival of the user at the station
        double acctakecost = 0;
        double accreturncost = 0;
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        stationPoint wp=way.get(0);
        if (wp.offsettimereached<=timeintervallforPrediction) {
            double timeoffset=timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getWalkTime());//wp.offsettimereached
            ProbabilityData pd=probutils.calculateAllTakeProbabilitiesWithArrival(wp.sd, wp.offsettimereached,timeoffset);
            //calculate takecost difference
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false,0, pd.probabilityTake,timeoffset);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTakeAfterTake,timeoffset);
            double extracosttake=(costtakeafter - costtake) ;
            //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 1, 0, hipodestination, newlookedlist, allstats, false, 0,pd.probabilityReturn,timeoffset);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnafterhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 1, 0, hipodestination, newlookedlist, allstats, false,0, pd.probabilityReturnAfterTake,timeoffset);
            double extracostreturn=(costreturnafterhip - costreturnhip) ;

            if (extracostreturn>0 || extracosttake<0){
                    System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.sd.getStation(), timeoffset);
            extracostreturn = extracostreturn* getReturnFactor(wp.sd.getStation(), timeoffset);;

            acctakecost+= wp.takeprob * extracosttake;
            accreturncost+= wp.takeprob * extracostreturn;
        }

        double globalcost = usercosttake + acctakecost + accreturncost;
        sd.setIndividualCost(usercosttake).setTakecostdiff(acctakecost).setReturncostdiff(accreturncost);
        return globalcost;

    }

    public double calculateCostsReturnAtStation(StationUtilityData sd, GeoPoint destination,
            List<StationUtilityData> allstats, double timeintervallforPrediction) {
        //return costs
        //take a close point to the station as hipotetical detsination
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercostreturn = calculateWayCostReturnHeuristic(way, sd, 1, sd.getBiketime(), destination, lookedlist, allstats, true,0, sd.getProbabilityReturn(),0);

        //analyze global costs
        double acctakecost = 0;
        double accreturncost = 0;
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        stationPoint wp=way.get(0);
        if (wp.offsettimereached<=timeintervallforPrediction) {
            double timeoffset=timeintervallforPrediction;//Math.max(timeintervallforPrediction, sd.getBiketime());//wp.offsettimereached
            ProbabilityData pd=probutils.calculateAllReturnProbabilitiesWithArrival(wp.sd, wp.offsettimereached,timeoffset);
            //calculate takecost difference
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTake,timeoffset);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTakeAfterRerturn,timeoffset);
            double extracosttake=(costtakeafter - costtake) ;
            //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 1, 0, hipodestination, newlookedlist, allstats, false, 0, pd.probabilityReturn,timeoffset);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnafterhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 1, 0, hipodestination, newlookedlist, allstats, false, 0, pd.probabilityReturnAfterReturn,timeoffset);
            double extracostreturn=(costreturnafterhip - costreturnhip) ;

            if (extracostreturn<0 || extracosttake>0){
                    System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() +  " " + extracosttake+ " " + extracostreturn );
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.sd.getStation(), timeoffset);
            extracostreturn = extracostreturn* getReturnFactor(wp.sd.getStation(),timeoffset);;

            acctakecost+= wp.returnprob * extracosttake;
            accreturncost+= wp.returnprob * extracostreturn;
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
                return Math.max(0,probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (4) :
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
                return Math.max(0,probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (4) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s,timeoffset);
        }
         return 1;
    }
}
