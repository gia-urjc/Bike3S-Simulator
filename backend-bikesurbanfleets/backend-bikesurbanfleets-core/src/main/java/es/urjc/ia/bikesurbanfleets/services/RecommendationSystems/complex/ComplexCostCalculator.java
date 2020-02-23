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
public class ComplexCostCalculator {

    //methods for cost calculations
    public ComplexCostCalculator(double marginprob, double maxcost, double unsuccostrent, double unsuccostret,
            double walkvel, double cycvel, double minsecondaryprob,
            UtilitiesProbabilityCalculator probutils,
            int PredictionNorm, double normmultiplier, double alfa) {
        minimumMarginProbability = marginprob;
        unsuccessCostRent = unsuccostrent;
        unsuccessCostReturn = unsuccostret;
        expectedwalkingVelocity = walkvel;
        expectedcyclingVelocity = cycvel;
        minProbSecondaryRecommendation = minsecondaryprob;
        this.probutils = probutils;
        abandonCost = maxcost;
        predictionNormalisation = PredictionNorm;
        this.normmultiplier = normmultiplier;
        this.alfa=alfa;

    }
    final double alfa;
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

    private double costRent(double oldwalktime,double time, double prob, int iteration) {
        double timecost=time;
        return timecost;
   /*     if (timecost>=unsuccessCostRent) return timecost;
        else timecost = prob*time+(1-prob)*unsuccessCostRent;//timecost;
        
        double ratio;
        if (oldwalktime<unsuccessCostRent) {
            ratio=1-(oldwalktime/unsuccessCostRent);
        } else ratio=0;
        double gama = Math.pow(alfa,(double)(iteration));
        double totalratio=gama;//(gama+ratio)/2.0D;

        if (iteration==0) return timecost;
        else {
            if (timecost>=unsuccessCostRent) return timecost;
            else return (totalratio * timecost) + ((1-totalratio) * unsuccessCostRent);
        }
  */  }

    private double costReturn(double oldbiketime,double wtime, double btime, double prob, int iteration) {
        double timecost=wtime+btime;
        return timecost;
   /*     if (timecost>=unsuccessCostReturn)  return timecost;
        else timecost= prob*(timecost)+(1-prob)*unsuccessCostReturn;//(timecost);
 
        double ratio;
        if (oldbiketime<unsuccessCostReturn) {
            ratio=1-(oldbiketime/unsuccessCostReturn);
        } else ratio=0;
        double gama = Math.pow(alfa,(double)(iteration));
        double totalratio=gama;//(gama+ratio)/2.0D;
        
        if (iteration==0) return timecost;
        else {
            if (timecost>=unsuccessCostReturn) return timecost;
            else return (totalratio * timecost) + ((1-totalratio) * unsuccessCostReturn);
        }
*/    }

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

        double oldwalktime=0;
        double accwalkdist = walkdist;
        double accwalktime = walkdist / expectedwalkingVelocity;
        double margprob = 1;
        double thisprob = takeprob;
        boolean end = false;
        double expectedcost = 0;
        double expectedunsucesses = 0;
        double abandonprob = 0;
        StationUtilityData current = sd;
        int iteration = -1;
        double gama;
        while (!end) {
            iteration++;
            double thisabsolutprob = margprob * thisprob;
            double thiscost = costRent(oldwalktime,accwalktime, thisprob, iteration);
            oldwalktime=accwalktime;
            double newmargprob = margprob - thisabsolutprob;
            if (newmargprob <= minimumMarginProbability) {
                way.add(new stationPoint(current, accwalktime, margprob, 0));
                expectedcost = expectedcost + margprob * thiscost;
                end = true;
            } else {
                expectedcost = expectedcost + (thisabsolutprob * thiscost);
                way.add(new stationPoint(current, accwalktime, thisabsolutprob, 0));

                //find best neighbour
                lookedlist.add(current);
                StationUtilityData closestneighbour = bestNeighbourRent(current.getStation(), lookedlist, allstats, accwalkdist, probtimeoffset, maxdistance);
                if (closestneighbour != null ){//&& iteration<1) {
                    //      if (BetterBestNeighbourRent(current,closestneighbour,probtimeoffset+accwalktime)) throw new BetterFirstStationException();
                    if (setneihbour) {
                        current.bestNeighbour = closestneighbour;
                        setneihbour = false;
                    }
                    accwalkdist = accwalkdist + current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                    accwalktime = accwalkdist / expectedwalkingVelocity;
                    thisprob = probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset + accwalktime);
                    expectedunsucesses = expectedunsucesses + newmargprob;
                    current = closestneighbour;
                    margprob = newmargprob;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostRent seconds
                    expectedcost = expectedcost + (newmargprob * unsuccessCostRent);
                    abandonprob = newmargprob;
                    end = true;
                }
            }
        }
        sd.setAbandonProbability(abandonprob);
        sd.setExpectedUnsucesses(expectedunsucesses);
        sd.setExpectedtimeIfNotAbandon(expectedcost);
        double totalcost = //(sd.getAbandonProbability()) * abandonCost;
  //              + (sd.getExpectedUnsucesses()) * unsuccessCostRent
                + sd.getExpectedtimeIfNotAbandon();
        sd.setTotalCost(totalcost);

        return totalcost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateWayCostReturnHeuristic(List<stationPoint> way, StationUtilityData sd,
            double bikedist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats,
            double returnprob, double probtimeoffset, boolean setneihbour) throws BetterFirstStationException {
        double accbikedist = bikedist;
        double accbiketime = accbikedist / expectedcyclingVelocity;
        double oldbiketime=0;
        double margprob = 1;
        double thisprob = returnprob;
        boolean end = false;
        double expectedcost = 0;
        double expectedunsucesses = 0;
        double abandonprob = 0;
        StationUtilityData current = sd;
        int iteration = -1;
        while (!end) {
            iteration++;
            double thisabsolutprob = margprob * thisprob;
            double walktime = current.getStation().getPosition().distanceTo(destination) / expectedwalkingVelocity;
            double thiscost = costReturn(oldbiketime,walktime, accbiketime, thisprob, iteration);
            oldbiketime=accbiketime;
            double newmargprob = margprob - thisabsolutprob;
            if (newmargprob <= minimumMarginProbability) {
                way.add(new stationPoint(current, accbiketime, 0, margprob));
                expectedcost = expectedcost + (margprob * thiscost);
                end = true;
            } else {
                expectedcost = expectedcost + (thisabsolutprob * thiscost);
                way.add(new stationPoint(current, accbiketime, 0, thisabsolutprob));

                //find best neighbour
                lookedlist.add(current);
                StationUtilityData closestneighbour = bestNeighbourReturn(current.getStation(), lookedlist, allstats, destination, accbikedist, probtimeoffset);
                if (closestneighbour != null ){// && iteration<1) {
                    //      if (BetterBestNeighbourReturn(current,closestneighbour,probtimeoffset+accbiketime)) throw new BetterFirstStationException();
                    if (setneihbour) {
                        current.bestNeighbour = closestneighbour;
                        setneihbour = false;
                    }
                    accbikedist = accbikedist + current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                    accbiketime = accbikedist / expectedcyclingVelocity;
                    thisprob = probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset + accbiketime);
                    current = closestneighbour;
                    expectedunsucesses = expectedunsucesses + newmargprob;
                    margprob = newmargprob;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostReturn seconds
                    abandonprob = newmargprob;
                    expectedcost = expectedcost + (newmargprob * unsuccessCostReturn);
                    end = true;
                }
            }
        }
        sd.setAbandonProbability(abandonprob);
        sd.setExpectedUnsucesses(expectedunsucesses);
        sd.setExpectedtimeIfNotAbandon(expectedcost);
        double totalcost = //(sd.getAbandonProbability()) * abandonCost
  //              + (sd.getExpectedUnsucesses()) * unsuccessCostReturn
                + sd.getExpectedtimeIfNotAbandon();
        sd.setTotalCost(totalcost);
        return totalcost;
    }

    double unsuccessCostcomparison = 6000;

    double calcCostRentcomp(double time, double probability) {
        return time + (1 - probability) * unsuccessCostcomparison;
//        return time/probability;
    }

    double calcCostReturncomp(double walktime, double biketime, double probability) {
        return biketime + walktime + (1 - probability) * unsuccessCostcomparison;
//        return time/probability;
    }

    private boolean BetterBestNeighbourRent(StationUtilityData sd, StationUtilityData closestneighbour, double offset) {
        double p1 = probutils.calculateTakeProbability(sd.getStation(), offset + sd.getWalkTime());
        double c1 = calcCostRentcomp(sd.getWalkTime(), p1);
        double p2 = probutils.calculateTakeProbability(closestneighbour.getStation(), offset + closestneighbour.getWalkTime());
        double c2 = calcCostRentcomp(closestneighbour.getWalkTime(), p2);
        if (c2 < c1) {
            return true;
        }
        return false;
    }

    private boolean BetterBestNeighbourReturn(StationUtilityData sd, StationUtilityData closestneighbour, double offset) {
        double p1 = probutils.calculateReturnProbability(sd.getStation(), offset + sd.getBiketime());
        double c1 = calcCostReturncomp(sd.getWalkTime(), sd.getBiketime(), p1);
        double p2 = probutils.calculateReturnProbability(closestneighbour.getStation(), offset + closestneighbour.getBiketime());
        double c2 = calcCostReturncomp(closestneighbour.getWalkTime(), closestneighbour.getBiketime(), p2);
        if (c2 < c1) {
            return true;
        }
        return false;
    }

    public double calculateCostRentHeuristicNow(StationUtilityData sd, List<StationUtilityData> allstats, double maxdistance) throws BetterFirstStationException {
        double iter = calculateWayCostRentHeuristic(new ArrayList<>(), sd,
                sd.getWalkdist(), new ArrayList<>(), allstats, sd.getProbabilityTake(), 0, maxdistance, true);
        return iter;
    }

    //DO NOT CHANGE IT IS WORKING :)
    public double calculateCostReturnHeuristicNow(StationUtilityData sd,
            GeoPoint destination,
            List<StationUtilityData> allstats) throws BetterFirstStationException {
        double iter = calculateWayCostReturnHeuristic(new ArrayList<>(), sd,
                sd.getBikedist(), destination, new ArrayList<>(), allstats, sd.getProbabilityReturn(), 0, true);
        return iter;
    }

    //with probabilities recalculated at the correct time
    private StationUtilityData bestNeighbourRent(Station s, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats,
            double accwalkdistance, double probtimeoffset, double maxdistance) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double dist = s.getPosition().distanceTo(nei.getStation().getPosition());
                if (((accwalkdistance * STRAIGT_LINE_FACTOR_FOOT)*2.2D + dist) <= (maxdistance)) {
                    double newacctime = (accwalkdistance + dist) / expectedwalkingVelocity;
                    double rentprob = probutils.calculateTakeProbability(nei.getStation(), newacctime + probtimeoffset);
                    if (rentprob > 0 && rentprob > minProbSecondaryRecommendation) {
                        double thiscost = calcCostRentcomp(newacctime, rentprob);
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
                double returnprob = probutils.calculateReturnProbability(nei.getStation(), altthisbiketime + probtimeoffset);
                if (returnprob > 0 && returnprob > minProbSecondaryRecommendation) {
                    double altthiswalktime = nei.getStation().getPosition().distanceTo(destination) / expectedwalkingVelocity;
                    double thiscost = calcCostReturncomp(altthiswalktime, altthisbiketime, returnprob);
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

        stationPoint(StationUtilityData sd, double t, double tprob, double rprob) {
            this.sd = sd;
            offsettimereached = t;
            takeprob = tprob;
            returnprob = rprob;
        }
    }

    final double estimatedavwalkdistnearest = 150;
    final double estimatedavbikedistnearest = 1000;
    
    public double calculateCostRentDifference(
            StationUtilityData sd,
            double walkdist,
            List<StationUtilityData> allstats,
            double takeprob, double secondtakeprob, double probtimeoffset, double maxdistance) {
//version full
            return calculateRentDifference(sd, walkdist, allstats, takeprob, secondtakeprob, probtimeoffset, maxdistance);
 
// version simple
 //           double time=walkdist / expectedwalkingVelocity;
//            if (time >unsuccessCostRent) return 0;
//            return  (takeprob-secondtakeprob) * (unsuccessCostRent-time);
    }

    public double calculateCostReturnDifference(StationUtilityData sd,
            double bikedist, GeoPoint destination,
            List<StationUtilityData> allstats,
            double returnprob, double secondreturnpron, double probtimeoffset) {
//version full
        return  calculateReturnDifference(sd, bikedist, destination, allstats, returnprob, secondreturnpron, probtimeoffset);
// version simple
/*        double biketime = bikedist / expectedcyclingVelocity;
        double walktime = sd.getStation().getPosition().distanceTo(destination) / expectedwalkingVelocity;
        double time=walktime+biketime;
        if (time >unsuccessCostReturn) return 0;
        return  (returnprob-secondreturnpron) * (unsuccessCostReturn- time);
  */  }

    //global cost calculation. calculates the cost of taking/returning and also the cost differences
    // returns the global costs
    public double calculateCostsRentAtStation(StationUtilityData sd,
            List<StationUtilityData> allstats, double timeintervallforPrediction, double maxuserdistance, double maxDistanceRecomendationTake) throws BetterFirstStationException {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercosttake = calculateWayCostRentHeuristic(way, sd, sd.getWalkdist(), lookedlist, allstats, sd.getProbabilityTake(), 0, maxuserdistance, true);

        //analyze costs earnings in the timeintervall
        // we take the highest value between timeintervall and the potential arrival of the user at the station
        double acctakecost = 0;
        double accreturncost = 0;
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        for (stationPoint wp : way) 
    //    stationPoint wp=way.get(0);
        {
            //       if (wp.offsettimereached>timeintervallforPrediction) break;
            double timeoffset = Math.max(timeintervallforPrediction, wp.offsettimereached);//sd.getWalkTime());//wp.offsettimereached
            ProbabilityData pd = probutils.calculateAllTakeProbabilitiesWithArrival(wp.sd, timeoffset);
            //calculate takecost difference
            double extracosttake = calculateCostRentDifference(wp.sd, estimatedavwalkdistnearest, allstats, pd.probabilityTake, pd.probabilityTakeAfterTake, timeoffset, maxDistanceRecomendationTake);
            //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            double extracostreturn = calculateCostReturnDifference(wp.sd, estimatedavbikedistnearest, hipodestination, allstats, pd.probabilityReturn, pd.probabilityReturnAfterTake, timeoffset);

            if (extracostreturn > 0 || extracosttake < 0) {
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + sd.getStation().getId() + " " + extracosttake + " " + extracostreturn);
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.sd.getStation(), timeoffset);
            extracostreturn = extracostreturn * getReturnFactor(wp.sd.getStation(), timeoffset);;

            acctakecost += wp.takeprob * extracosttake;
            accreturncost += wp.takeprob * extracostreturn;
        }

        double globalcost = usercosttake + acctakecost +  0*accreturncost;
        sd.setIndividualCost(usercosttake).setTakecostdiff(acctakecost).setReturncostdiff(accreturncost).setTotalCost(globalcost);
        return globalcost;

    }

    public double calculateCostsReturnAtStation(StationUtilityData sd, GeoPoint destination,
            List<StationUtilityData> allstats, double timeintervallforPrediction, double maxDistanceRecomendationTake) throws BetterFirstStationException {
        //return costs
        //take a close point to the station as hipotetical detsination
        List<StationUtilityData> lookedlist = new ArrayList<>();
        List<stationPoint> way = new LinkedList<stationPoint>();
        double usercostreturn = calculateWayCostReturnHeuristic(way, sd, sd.getBikedist(), destination, lookedlist, allstats, sd.getProbabilityReturn(), 0, true);

        //analyze global costs
        double acctakecost = 0;
        double accreturncost = 0;
        lookedlist.clear();
        List<StationUtilityData> newlookedlist = new ArrayList<>();
        for (stationPoint wp : way) 
        //stationPoint wp=way.get(0);
        {
            //          if (wp.offsettimereached>timeintervallforPrediction) break;
            double timeoffset = Math.max(timeintervallforPrediction, wp.offsettimereached);//sd.getBiketime());//wp.offsettimereached
            ProbabilityData pd = probutils.calculateAllReturnProbabilitiesWithArrival(wp.sd, timeoffset);
            //calculate takecost difference
            double   extracosttake = calculateCostRentDifference(wp.sd, estimatedavwalkdistnearest, allstats, pd.probabilityTake, pd.probabilityTakeAfterRerturn, timeoffset, maxDistanceRecomendationTake);
              //calculate return cost difference
            GeoPoint hipodestination = wp.sd.getStation().getPosition();
            double extracostreturn = calculateCostReturnDifference(wp.sd, estimatedavbikedistnearest, hipodestination, allstats, pd.probabilityReturn, pd.probabilityReturnAfterReturn, timeoffset);

            if (extracostreturn < 0 || extracosttake > 0) {
                System.out.println("EEEEERRRRROOOOORRRR: invalid cost station in return  " + sd.getStation().getId() + " " + extracosttake + " " + extracostreturn);
            }
            //normalize the extracost
            extracosttake = extracosttake * getTakeFactor(wp.sd.getStation(), timeoffset);
            extracostreturn = extracostreturn * getReturnFactor(wp.sd.getStation(), timeoffset);;

            acctakecost += wp.returnprob * extracosttake;
            accreturncost += wp.returnprob * extracostreturn;
        }

        double globalcost = usercostreturn + acctakecost + 0.5* accreturncost;
        sd.setIndividualCost(usercostreturn).setTakecostdiff(acctakecost).setReturncostdiff(accreturncost).setTotalCost(globalcost);
        return globalcost;
    }
 
    private double calculateRentDifference(
            StationUtilityData sd,
            double walkdist,
            List<StationUtilityData> allstats,
            double takeprob, double secondtakeprob, double probtimeoffset, double maxdistance) {

        List<StationUtilityData> lookedlist= new ArrayList<>();
        boolean end=false;
        double accwalkdistonlyneighbour=walkdist;
        double accwalktime=0;
        double thisprob;
        double margenprob=1D;
        double cost=0;
        StationUtilityData current = sd;
        while (!end) {
            lookedlist.add(current);
            StationUtilityData closestneighbour = bestNeighbourRent(current.getStation(), lookedlist, allstats, accwalkdistonlyneighbour, probtimeoffset, maxdistance);
            if (closestneighbour != null ){
                double dist = current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                accwalkdistonlyneighbour=accwalkdistonlyneighbour+dist;
                accwalktime = accwalkdistonlyneighbour / expectedwalkingVelocity;
                thisprob = probutils.calculateTakeProbability(closestneighbour.getStation(), probtimeoffset + accwalktime);
                cost=cost+(margenprob*(dist/ expectedwalkingVelocity));
                margenprob=margenprob*(1-thisprob);
                current = closestneighbour;
                if (margenprob <= minimumMarginProbability) {
                    end = true;
                }
            } else { //if no best neigbour found 
                cost = cost + (margenprob * unsuccessCostRent);
                end = true;
            }
        }
        return (takeprob-secondtakeprob)*cost;
    }

    private double calculateReturnDifference(
            StationUtilityData sd,
            double bikedist, GeoPoint destination,
            List<StationUtilityData> allstats,
            double returnprob, double secondreturnprob, double probtimeoffset) {

        List<StationUtilityData> lookedlist= new ArrayList<>();
        boolean end=false;
        double accbikedistonlyneighbour=bikedist;
        double accbiketime=0;
        double thisprob;
        double margenprob=1D;
        double cost=0;
        StationUtilityData current = sd;
        while (!end) {
            lookedlist.add(current);
            StationUtilityData closestneighbour = bestNeighbourReturn(current.getStation(), lookedlist, allstats, destination, accbikedistonlyneighbour, probtimeoffset);
            if (closestneighbour != null ){
                double dist = current.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
                accbikedistonlyneighbour=accbikedistonlyneighbour+dist;
                accbiketime = accbikedistonlyneighbour / expectedcyclingVelocity;
                thisprob = probutils.calculateReturnProbability(closestneighbour.getStation(), probtimeoffset + accbiketime);
                double walktime = closestneighbour.getStation().getPosition().distanceTo(destination) / expectedwalkingVelocity;
                cost=cost + margenprob * (dist/ expectedcyclingVelocity) + margenprob * thisprob * walktime;
                margenprob=margenprob*(1-thisprob);
                current = closestneighbour;
                if (margenprob <= minimumMarginProbability) {
                    end = true;
                }
            } else { //if no best neigbour found 
                cost = cost + (margenprob * unsuccessCostReturn);
                end = true;
            }
        }
        return (returnprob-secondreturnprob)*cost;
    }

    private double getTakeFactor(Station s, double timeoffset) {
        double fixedmult = normmultiplier;
        double takerate = probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double probtake = probutils.calculateProbabilityAtLeast1UserArrivingForTake(s, timeoffset);
        double diff = Math.max(0, probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                - probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        double takeonlyprob = probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s, timeoffset);;
        double takeexpected = probutils.calculateExpectedTakes(s, timeoffset);

        /*     System.out.println("take Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "takerate " + takerate + " " + 
               "probtake " + probtake + " " + 
               "diff " + diff + " " + 
                "takeonlyprob " + takeonlyprob + " "  +
                 "takeexpected " + takeexpected + " "  +
              "retu rate " +probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " "  +
                "take rate " +probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " " 
               );
         */ switch (predictionNormalisation) {
            case (0):
                return normmultiplier;
            case (1):
                return normmultiplier * probutils.calculateProbabilityAtLeast1UserArrivingForTake(s, timeoffset);
            case (2):
                return normmultiplier * Math.max(0, probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        - probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (3):
                return normmultiplier * (probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        + probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)) / 2;
            case (4):
                return normmultiplier * probutils.calculateExpectedTakes(s, timeoffset);
            case (5):
                return normmultiplier * (((probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        + probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)) / 2)
                        + (probutils.calculateProbabilityAtLeast1UserArrivingForTake(s, timeoffset)));
            case (6):
                return normmultiplier * probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (7):
                return normmultiplier * probutils.calculateProbabilityAtLeast1UserArrivingForTakeOnlyTakes(s, timeoffset);
        }
        return 1;
    }

    private double getReturnFactor(Station s, double timeoffset) {
        double fixedmult = normmultiplier;
        double returnrate = probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
        double probreturn = probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s, timeoffset);
        double diff = Math.max(0, probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                - probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
        double returnonlyprob = probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s, timeoffset);
        double returnexpected = probutils.calculateExpectedReturns(s, timeoffset);

        /*       System.out.println("retu Station avb/avs " + s.getId() + " " + s.availableBikes()+ "/"+ s.availableSlots() + " " +
               "fixedmult " + fixedmult + " " + 
               "returate " + returnrate + " " + 
               "probretu " + probreturn + " " + 
               "diff " + diff + " " + 
                "retuonlyprob " + returnonlyprob + " "  +
                "retuexpected " + returnexpected + " "  +
                "retu rate " +probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " "  +
                "take rate " +probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)+ " " 
                
               );
         */ switch (predictionNormalisation) {
            case (0):
                return normmultiplier;
            case (1):
                return normmultiplier * probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s, timeoffset);
            case (2):
                return normmultiplier * Math.max(0, probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        - probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset));
            case (3):
                return normmultiplier * (probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        + probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)) / 2;
            case (4):
                return normmultiplier * probutils.calculateExpectedReturns(s, timeoffset);
            case (5):
                return normmultiplier * (((probutils.dm.getStationTakeRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)
                        + probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset)) / 2)
                        + (probutils.calculateProbabilityAtLeast1UserArrivingForReturn(s, timeoffset)));
            case (6):
                return normmultiplier * probutils.dm.getStationReturnRateIntervall(s.getId(), SimulationDateTime.getCurrentSimulationDateTime(), timeoffset);
            case (7):
                return normmultiplier * probutils.calculateProbabilityAtLeast1UserArrivingForReturnOnlyReturns(s, timeoffset);
        }
        return 1;
    }
}
