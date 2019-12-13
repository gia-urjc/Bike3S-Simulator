/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import static es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters.STRAIGT_LINE_FACTOR_FOOT;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ProbabilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.xmlgraphics.image.codec.png.PNGChunk;

/**
 *
 * @author holger
 */
public class ComplexCostCalculatorOld {
    

    //methods for cost calculations
    public ComplexCostCalculatorOld(double marginprob, double maxcost, double unsuccostrent, double unsuccostret,
            double walkvel, double cycvel, double minsecondaryprob,
            UtilitiesProbabilityCalculator probutils,
            int PredictionNorm, double normmultiplier) {
        minimumMarginProbability = marginprob;
        unsuccessCostRent = unsuccostrent;
        unsuccessCostReturn = unsuccostret;
        expectedwalkingVelocity=walkvel;
        expectedcyclingVelocity=cycvel;
        minProbSecondaryRecommendation=minsecondaryprob;
        this.probutils=probutils;
        abandonCost=maxcost;
        predictionNormalisation=PredictionNorm;
        this.normmultiplier=normmultiplier;
        
    }

    final double normmultiplier;
    final int predictionNormalisation;
    final double minimumMarginProbability;
    final double unsuccessCostRent;
    final double unsuccessCostReturn;
    final double expectedwalkingVelocity;
    final double expectedcyclingVelocity;
    final double minProbSecondaryRecommendation;
    final double abandonCost; 
    UtilitiesProbabilityCalculator probutils;

    //claculates a "good" way and its cost for taking a bike
    //this is a sequence of stantions where the user can take a bike up to a probabilkity of 1-minimumMarginProbability
    //probtimeoffset is a timeoffset for calculating cost in the future
    //e.g. calculate the cost for getting a bike if the user appears at a walkingtime distances in 10 minutes (values are in seconds)
    private double calculateWayCostRentHeuristic(
            List<stationPoint> way, StationUtilityData sd, 
            double walkdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, 
            double takeprob, double probtimeoffset, double maxdistance, boolean setneihbour) throws BetterFirstStationException {
        
        double accwalkdist=walkdist;
        double accwalktime=accwalkdist/expectedwalkingVelocity;
        double margprob=1;
        boolean end=false;
        double thisabsolutprob;
        double thistakeprob=takeprob; 
        double totalcost=0;
        StationUtilityData current=sd;
        double extracost=0;
        while (!end) {
            thisabsolutprob=margprob*thistakeprob;
            double newmargprob=margprob-thisabsolutprob;
            double thiscost=accwalktime+ extracost;
            if (newmargprob<= minimumMarginProbability) {
                way.add(new stationPoint(current,accwalktime, margprob-minimumMarginProbability, 0));
                totalcost=totalcost+(margprob-minimumMarginProbability)*thiscost;
                end=true;
            } else {
                totalcost=totalcost+(thisabsolutprob*thiscost);
                way.add(new stationPoint(current,accwalktime, thisabsolutprob, 0));
                
                //find best neighbour
                lookedlist.add(current);
                StationUtilityData closestneighbour = bestNeighbourRent(current.getStation(), lookedlist, allstats,accwalkdist, probtimeoffset, maxdistance);
                if (closestneighbour!=null) {
            //      if (BetterBestNeighbourRent(current,closestneighbour,probtimeoffset+accwalktime)) throw new BetterFirstStationException();
                    if (setneihbour) {
                        current.bestNeighbour = closestneighbour;
                        setneihbour=false;
                    }
                    accwalkdist = accwalkdist+current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                    accwalktime=accwalkdist/expectedwalkingVelocity;
                    thistakeprob=probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset+accwalktime);
                    extracost=extracost+unsuccessCostRent;
                    current=closestneighbour;
                    margprob=newmargprob;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostRent seconds
                    totalcost=totalcost+(newmargprob-minimumMarginProbability)*(abandonCost+thiscost);
                    end=true;
                }    
            }
        }
        return totalcost;
     }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateWayCostReturnHeuristic(List<stationPoint> way, StationUtilityData sd,
            double bikedist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, 
            double returnprob, double probtimeoffset, boolean setneihbour) throws BetterFirstStationException {
        double accbikedist=bikedist;
        double accbiketime=accbikedist/expectedcyclingVelocity;
        double margprob=1;
        boolean end=false;
        double thisabsolutprob;
        double thisretprob=returnprob; 
        double totalcost=0;
        double extracost=0;
        StationUtilityData current=sd;
        while (!end) {
            thisabsolutprob=margprob*thisretprob;
            double newmargprob=margprob-thisabsolutprob;
            double walktime = current.getStation().getPosition().distanceTo(destination)/ expectedwalkingVelocity;
            double thiscost=walktime+accbiketime + extracost;
            if (newmargprob<= minimumMarginProbability) {
                way.add(new stationPoint(current,accbiketime, 0,margprob-minimumMarginProbability));
                totalcost=totalcost+(margprob-minimumMarginProbability)*thiscost;
                end=true;
            } else {
                totalcost=totalcost+(thisabsolutprob*thiscost);
                way.add(new stationPoint(current,accbiketime, 0,thisabsolutprob));
                
                //find best neighbour
                lookedlist.add(current);
                StationUtilityData closestneighbour = bestNeighbourReturn(current.getStation(), lookedlist, allstats,destination,accbikedist, probtimeoffset);
                if (closestneighbour!=null) {
            //      if (BetterBestNeighbourReturn(current,closestneighbour,probtimeoffset+accbiketime)) throw new BetterFirstStationException();
                    if (setneihbour) {
                        current.bestNeighbour = closestneighbour;
                        setneihbour=false;
                    }
                    accbikedist = accbikedist+current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                    accbiketime=accbikedist/expectedcyclingVelocity;
                    thisretprob=probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset+accbiketime);
                    current=closestneighbour;
                    extracost=extracost+unsuccessCostReturn;
                    margprob=newmargprob;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostReturn seconds
                    totalcost=totalcost+(newmargprob-minimumMarginProbability)*(abandonCost+extracost+accbiketime);
                    end=true;
                }    
            }
        }
        return totalcost;
    }
    
    double calcCostRentcomp(double time, double probability){
        return time+(1-probability)*unsuccessCostRent;
//        return time/probability;
    }
    double calcCostReturncomp(double walktime, double biketime,double probability){
        return biketime+ probability*walktime +(1-probability)*unsuccessCostReturn;
//        return time/probability;
    }
 
    
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
        double c1=calcCostReturncomp(sd.getWalkTime(), sd.getBiketime(), p1);
        double p2=probutils.calculateReturnProbability(closestneighbour.getStation(), offset+closestneighbour.getBiketime()) ;
        double c2=calcCostReturncomp(closestneighbour.getWalkTime(),closestneighbour.getBiketime(),p2);
        if (c2<c1) return true;
        return false;
    }
 
    public double calculateCostRentHeuristicNow(StationUtilityData sd, List<StationUtilityData> allstats, double maxdistance) throws BetterFirstStationException{
        double iter= calculateWayCostRentHeuristic(new ArrayList<>(), sd,
            sd.getWalkdist(), new ArrayList<>(), allstats, sd.getProbabilityTake(),0, maxdistance, true);
        return iter;
    }
    //DO NOT CHANGE IT IS WORKING :)
    public double calculateCostReturnHeuristicNow(StationUtilityData sd,
            GeoPoint destination,            
            List<StationUtilityData> allstats) throws BetterFirstStationException {
        double iter= calculateWayCostReturnHeuristic(new ArrayList<>(), sd,
            sd.getBikedist(), destination,new ArrayList<>(),allstats, sd.getProbabilityReturn(),0, true);
        return iter;
    }
     
    //with probabilities recalculated at the correct time
    private StationUtilityData bestNeighbourRent(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalkdistance, double probtimeoffset, double maxdistance) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double dist=s.getPosition().distanceTo(nei.getStation().getPosition());
                if (((accwalkdistance*STRAIGT_LINE_FACTOR_FOOT) +dist)<= (maxdistance*1.3)) {
                    double newacctime=(accwalkdistance+dist)/expectedwalkingVelocity ;
                    double rentprob=probutils.calculateTakeProbability(nei.getStation(), newacctime+probtimeoffset);
                    if (rentprob > 0 && rentprob > minProbSecondaryRecommendation) {
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
            double accbikedistance, double probtimeoffset) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double altthisbiketime = (accbikedistance + s.getPosition().distanceTo(nei.getStation().getPosition())) / expectedcyclingVelocity;
                double returnprob=probutils.calculateReturnProbability(nei.getStation(), altthisbiketime+probtimeoffset);
                if (returnprob > 0 &&  returnprob > minProbSecondaryRecommendation) {
                    double altthiswalktime = nei.getStation().getPosition().distanceTo(destination) / expectedwalkingVelocity;
                    double thiscost=calcCostReturncomp(altthiswalktime, altthisbiketime ,returnprob);
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
            List<StationUtilityData> allstats, double timeintervallforPrediction, double maxuserdistance, double maxDistanceRecomendationTake) throws BetterFirstStationException{
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercosttake = calculateWayCostRentHeuristic(way, sd , sd.getWalkdist(), lookedlist, allstats, sd.getProbabilityTake(),0, maxuserdistance,true);

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
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 0, newlookedlist, allstats, pd.probabilityTake,timeoffset, maxDistanceRecomendationTake, false);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 0, newlookedlist, allstats, pd.probabilityTakeAfterTake,timeoffset, maxDistanceRecomendationTake, false);
            double extracosttake=(costtakeafter - costtake) ;
            //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 0, hipodestination, newlookedlist, allstats, pd.probabilityReturn,timeoffset, false);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnafterhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 0, hipodestination, newlookedlist, allstats, pd.probabilityReturnAfterTake,timeoffset, false);
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
            List<StationUtilityData> allstats, double timeintervallforPrediction, double maxDistanceRecomendationTake) throws BetterFirstStationException{
        //return costs
        //take a close point to the station as hipotetical detsination
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercostreturn = calculateWayCostReturnHeuristic(way, sd, sd.getBikedist(), destination, lookedlist, allstats, sd.getProbabilityReturn(),0, true);

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
            double costtake = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 0, newlookedlist, allstats,  pd.probabilityTake,timeoffset, maxDistanceRecomendationTake, false);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costtakeafter = calculateWayCostRentHeuristic(new ArrayList<>(), wp.sd, 0, newlookedlist, allstats,  pd.probabilityTakeAfterRerturn,timeoffset, maxDistanceRecomendationTake, false);
            double extracosttake=(costtakeafter - costtake) ;
            //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 0, hipodestination, newlookedlist, allstats,  pd.probabilityReturn,timeoffset, false);
            newlookedlist=new ArrayList<>();//holger(lookedlist);
            double costreturnafterhip = calculateWayCostReturnHeuristic(new ArrayList<>(),wp.sd, 0, hipodestination, newlookedlist, allstats,pd.probabilityReturnAfterReturn,timeoffset, false);
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
