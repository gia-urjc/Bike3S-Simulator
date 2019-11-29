/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import static es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters.STRAIGT_LINE_FACTOR;
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
public class ComplexCostCalculator2Bis {
    

    //methods for cost calculations
    public ComplexCostCalculator2Bis(double marginprob, double maxcost, double unsuccostrent, double unsuccostret,
            double penalfactorrent, double penalfactorret, double walkvel, double cycvel, double minsecondaryprob,
            double maxDistanceRecomendation, UtilitiesProbabilityCalculator probutils,
            int PredictionNorm, double normmultiplier) {
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
        predictionNormalisation=PredictionNorm;
        this.normmultiplier=normmultiplier;
    }

    final double normmultiplier;
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

    private double calcCostRent(double time, double probability){
        if (time>unsuccessCostRent) return time+(1-probability)*2*time;
        else return time+(1-probability)*unsuccessCostRent;
//        return time;
  //      if (time>unsuccessCostRent) return unsuccessCostRent;
  //      else return time+(1-probability)*unsuccessCostRent;
//        return time/probability;
    }
    private double calcCostReturn(double time, double probability){
        if (time>unsuccessCostReturn) return time+(1-probability)*2*time;
        else return time+(1-probability)*unsuccessCostReturn;
//        return time;
    //    if (time>unsuccessCostReturn) return unsuccessCostReturn;
    //    else return time+(1-probability)*unsuccessCostReturn;
//        return time/probability;
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
            double takeprob, double probtimeoffset, double maxdistance) throws BetterFirstStationException {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        double newaccwalktime=accwalktime+walktime;
        double thisprob=margprob * takeprob;
        double newmargprob = margprob -thisprob;
        double thiscost= calcCostRent(newaccwalktime, takeprob);

        if (newmargprob <= minimumMarginProbability) {
            way.add(new stationPoint(sd,newaccwalktime, margprob-minimumMarginProbability, 0));
            return (margprob-minimumMarginProbability)*thiscost;
        }
        thiscost=thiscost*thisprob;
        way.add(new stationPoint(sd,newaccwalktime, thisprob, 0));
       //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), lookedlist, allstats,newaccwalktime, probtimeoffset, maxdistance);
        double margcost;
        if (closestneighbour!=null) {
     //       if (BetterBestNeighbourRent(sd,closestneighbour,probtimeoffset+accwalktime)) throw new BetterFirstStationException();
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double newtime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/ walkingVelocity;
            double neighbourrentprob=probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset+newaccwalktime+newtime);
            margcost = calculateWayCostRentHeuristic(way, closestneighbour, newmargprob, newtime, lookedlist, allstats, false, newaccwalktime,neighbourrentprob, probtimeoffset, maxdistance);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostRent seconds
            margcost = (newmargprob - minimumMarginProbability) * calcCostRent(unsuccessCostRent, 1);
        }
        return thiscost + margcost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateWayCostReturnHeuristic(List<stationPoint> way, StationUtilityData sd,
            double margprob, double biketime, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start,double accbiketime,
            double returnprob, double probtimeoffset) throws BetterFirstStationException {
        if (margprob <= minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        double newaccbiketime=accbiketime+biketime;

        double thisprob=margprob * returnprob;
        double newmargprob = margprob -thisprob;
        double walktime = sd.getStation().getPosition().distanceTo(destination)/ walkingVelocity;
        double thiscost=calcCostReturn(newaccbiketime+walktime,returnprob) ;
        
        if (newmargprob <= minimumMarginProbability) {
            way.add(new stationPoint(sd,newaccbiketime,0, margprob-minimumMarginProbability));
            return (margprob-minimumMarginProbability)*(thiscost);
        }
        way.add(new stationPoint(sd,newaccbiketime,0, thisprob));
        thiscost= thiscost * thisprob;
        // find best neigbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), lookedlist, allstats, destination, newaccbiketime, probtimeoffset);
        double margcost;
        if (closestneighbour!=null) {
//            if (BetterBestNeighbourReturn(sd,closestneighbour,probtimeoffset+accbiketime)) throw new BetterFirstStationException();
            if (start) {
                sd.bestNeighbour = closestneighbour;
            }
            double newbiketime = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) / cyclingVelocity;
            double neighbourreturnprob=probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset+newaccbiketime+newbiketime);
            margcost = calculateWayCostReturnHeuristic(way, closestneighbour, newmargprob, newbiketime, destination, lookedlist, allstats, false, newaccbiketime,neighbourreturnprob,probtimeoffset);
        } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostReturn seconds
            margcost = (newmargprob - minimumMarginProbability) * calcCostReturn(unsuccessCostReturn, 1);
        }
        return thiscost + margcost;
    }
    double calcCostRentcomp(double time, double probability){
        if (time>unsuccessCostRent) return time+(1-probability)*2*time;
        else return time+(1-probability)*unsuccessCostRent;
//        return time/probability;
    }
    double calcCostReturncomp(double time, double probability){
        if (time>unsuccessCostReturn) return time+(1-probability)*2*time;
        else return time+(1-probability)*unsuccessCostReturn;
//        return time/probability;
    }
  
    
    double HIGH_VALUE=100000;
    private boolean BetterBestNeighbourRent(StationUtilityData sd,StationUtilityData closestneighbour, double offset){
        double p1=probutils.calculateTakeProbability(sd.getStation(), offset+sd.getWalkTime()) ;
        double c1=calcCostRentcomp(sd.getWalkTime(),p1);
        double p2=probutils.calculateTakeProbability(closestneighbour.getStation(), offset+closestneighbour.getWalkTime()) ;
        double c2=calcCostRentcomp(closestneighbour.getWalkTime(),p2);
        if (c2<c1) return true;
        return false;
    }
    private boolean BetterBestNeighbourReturn(StationUtilityData sd,StationUtilityData closestneighbour, double offset){
        double p1=probutils.calculateReturnProbability(sd.getStation(), offset+sd.getBiketime()) ;
        double c1=calcCostReturncomp((sd.getBiketime()+sd.getWalkTime()), p1);
        double p2=probutils.calculateReturnProbability(closestneighbour.getStation(), offset+closestneighbour.getBiketime()) ;
        double c2=calcCostReturncomp((closestneighbour.getBiketime()+closestneighbour.getWalkTime()),p2);
        if (c2<c1) return true;
        return false;
    }
 
    public double calculateCostRentHeuristicNow(StationUtilityData sd, List<StationUtilityData> allstats, double maxdistance) throws BetterFirstStationException{
        return calculateWayCostRentHeuristic(new ArrayList<>(), sd,
            1, sd.getWalkTime(), new ArrayList<>(), allstats, true,0,sd.getProbabilityTake(),0, maxdistance);
    }
    //DO NOT CHANGE IT IS WORKING :)
    public double calculateCostReturnHeuristicNow(StationUtilityData sd,
            GeoPoint destination,            
            List<StationUtilityData> allstats) throws BetterFirstStationException {
        return calculateWayCostReturnHeuristic(new ArrayList<>(), sd,
            1, sd.getBiketime(), destination,new ArrayList<>(),allstats,  true, 0, sd.getProbabilityReturn(),0);
    }
    
    //with probabilities recalculated at the correct time
    private StationUtilityData bestNeighbourRent(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalktime, double probtimeoffset, double maxdistance) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double accdist=accwalktime*walkingVelocity*STRAIGT_LINE_FACTOR + s.getPosition().distanceTo(nei.getStation().getPosition());
                double newacctime=accwalktime+(s.getPosition().distanceTo(nei.getStation().getPosition())/ walkingVelocity) ;
                if (accdist<= (maxdistance*1)) {
                    double rentprob=probutils.calculateTakeProbability(nei.getStation(), newacctime+probtimeoffset);
                    if (rentprob > minProbSecondaryRecommendation) {
                        double thiscost=calcCostRentcomp(newacctime,rentprob);
                        //calculate the cost of this potential neighbour
                        if (thiscost < newbestValueFound) {
                            newbestValueFound = thiscost;
                            bestneighbour = nei;
                        }
                    }
                }
            }
        }
        return bestneighbour;
    }

    //with probs recalculated at the correct time
    private StationUtilityData bestNeighbourReturn(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats, GeoPoint destination,
            double accbiketime, double probtimeoffset) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double altthisbiketime = accbiketime + s.getPosition().distanceTo(nei.getStation().getPosition()) / cyclingVelocity;
                double returnprob=probutils.calculateReturnProbability(nei.getStation(), altthisbiketime+probtimeoffset);
                if (returnprob > minProbSecondaryRecommendation) {
                    double altthiswalktime = nei.getStation().getPosition().distanceTo(destination) / walkingVelocity;
                    double thiscost=calcCostReturncomp(altthisbiketime+ altthiswalktime,returnprob);
                    if (thiscost < newbestValueFound) {
                        newbestValueFound = thiscost;
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
            List<StationUtilityData> allstats, double timeintervallforPrediction, double maxdistance ) throws BetterFirstStationException{
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercosttake = calculateWayCostRentHeuristic(way, sd , 1, sd.getWalkTime(), lookedlist, allstats, true, 0, sd.getProbabilityTake(),0, maxdistance);

        //analyze costs earnings in the timeintervall
        // we take the highest value between timeintervall and the potential arrival of the user at the station
        double acctakecost = 0;
        double accreturncost = 0;
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        for (stationPoint wp : way) {
     //       if (wp.offsettimereached>timeintervallforPrediction) break;
            double timeoffset=Math.max(timeintervallforPrediction, wp.offsettimereached);//sd.getWalkTime());//wp.offsettimereached
            ProbabilityData pd=probutils.calculateAllTakeProbabilitiesWithArrival(wp.sd, sd.getWalkTime(),timeoffset);
            //calculate takecost difference
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false,0, pd.probabilityTake,timeoffset, maxDistanceRecomendation);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTakeAfterTake,timeoffset, maxDistanceRecomendation);
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
            List<StationUtilityData> allstats, double timeintervallforPrediction) throws BetterFirstStationException{
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
        for (stationPoint wp : way) {
  //          if (wp.offsettimereached>timeintervallforPrediction) break;
            double timeoffset=Math.max(timeintervallforPrediction, wp.offsettimereached);//sd.getBiketime());//wp.offsettimereached
            ProbabilityData pd=probutils.calculateAllReturnProbabilitiesWithArrival(wp.sd, sd.getBiketime(),timeoffset);
            //calculate takecost difference
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTake,timeoffset, maxDistanceRecomendation);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 1, 0, newlookedlist, allstats, false, 0, pd.probabilityTakeAfterRerturn,timeoffset, maxDistanceRecomendation);
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
                return normmultiplier;
            case (1) :
                return probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForTake(s,timeoffset);
            case (3) :
                return Math.max(0,probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        }
         return 1;
    }
     private double getReturnFactor(Station s, double timeoffset){
        switch(predictionNormalisation){
            case (0) :
                 return normmultiplier;
            case (1) :
                return probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (2) :
                return probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s,timeoffset);
            case (3) :
                return Math.max(0,probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)-
                       probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        }
         return 1;
    }
}
