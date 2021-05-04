/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;

/**
 *
 * @author holger
 */
public class ComplexCostCalculatorNew {

    //methods for cost calculations
     public ComplexCostCalculatorNew(double minmarginprob, 
            double unsuccostrent, double unsuccostreturn,
            double walkvel, double cycvel, double minsecondaryprob,
            UtilitiesProbabilityCalculator probutils,
            GraphManager gm) {
        minimumMarginProbability = minmarginprob;
        unsuccessCostRent = unsuccostrent;
        unsuccessCostReturn = unsuccostreturn;
        expectedwalkingVelocity = walkvel;
        expectedcyclingVelocity = cycvel;
        minProbSecondaryRecommendation = minsecondaryprob;
        this.probutils = probutils;
        this.graphManager = gm;
    }
    GraphManager graphManager;
    final double minimumMarginProbability;
    final double unsuccessCostRent;
    final double unsuccessCostReturn;
    final double expectedwalkingVelocity;
    final double expectedcyclingVelocity;
    final double minProbSecondaryRecommendation;
    UtilitiesProbabilityCalculator probutils;

    private double costRent(double walktime, double prob) {
        return walktime;
    }

    private double costReturn(double wtime, double btime, double prob) {
        return btime + prob * wtime;
    }

    //claculates a "good" way and its cost for taking a bike
    //this is a sequence of stantions where the user can take a bike up to a probabilkity of 1-minimumMarginProbability
    //probtimeoffset is a timeoffset for calculating cost in the future
    //e.g. calculate the cost for getting a bike if the user appears at a walkingtime distances in 10 minutes (values are in seconds)
    public double calculateWayCostRentHeuristic(
            List<stationPoint> way, StationData sd,
            double walkdist,
            List<Station> lookedlist,
            List<Station> allstats,
            double takeprob, double probtimeoffset, double maxdistance, 
            boolean setneihbour, int maxneighbours) {

        double accwalkdist = walkdist;
        double accwalktime = walkdist / expectedwalkingVelocity;
        double currentwalktime = accwalktime;
        double margprob = 1;
        double thisprob = takeprob;
        boolean end = false;
        double expectedcost = 0;
        double abandonprob = 0;
        Station current = sd.station;
        int neighbours = 0;
        while (!end) {
            double thisabsolutprob = margprob * thisprob;
            double thiscost = costRent(currentwalktime, thisprob);
            double newmargprob = margprob - thisabsolutprob;
            expectedcost = expectedcost + margprob * thiscost;
            if (newmargprob <= minimumMarginProbability) {
                way.add(new stationPoint(current, accwalktime, margprob, 0));
                end = true;
            } else {
                way.add(new stationPoint(current, accwalktime, thisabsolutprob, 0));
                //find best neighbour
                lookedlist.add(current);
                Station closestneighbour = bestNeighbourRent(thisprob, current, lookedlist, allstats, accwalkdist, probtimeoffset, maxdistance);
                if (closestneighbour != null && neighbours<maxneighbours) {
                    double newdist = graphManager.estimateDistance(current.getPosition(), closestneighbour.getPosition(), "foot");
                    accwalkdist = accwalkdist + newdist;
                    accwalktime = accwalkdist / expectedwalkingVelocity;
                    thisprob = probutils.calculateTakeProbability(closestneighbour, probtimeoffset + accwalktime);
                    if (setneihbour) {
                        sd.bestNeighbour = closestneighbour;
                        sd.bestNeighbourReturnWalktime = 0;
                        sd.bestNeighbourProbability = thisprob;
                        setneihbour = false;
                    }
                    current = closestneighbour;
                    currentwalktime = newdist / expectedwalkingVelocity;
                    margprob = newmargprob;
                    neighbours++;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostRent seconds
                    if (thiscost>unsuccessCostRent) expectedcost = expectedcost + (newmargprob * thiscost);
                    else expectedcost = expectedcost + (newmargprob * unsuccessCostRent);
                    abandonprob = newmargprob;
                    end = true;
                }
            }
        }
        sd.abandonProbability = abandonprob;
        sd.expectedTimeIfNotAbandon = expectedcost;
        sd.totalCost = expectedcost;

        return sd.totalCost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    public double calculateWayCostReturnHeuristic(List<stationPoint> way, StationData sd,
            double bikedist, GeoPoint destination,
            List<Station> lookedlist,
            List<Station> allstats,
            double returnprob, double probtimeoffset, boolean setneihbour,
            int maxneighbours) {
        double accbikedist = bikedist;
        double accbiketime = accbikedist / expectedcyclingVelocity;
        double currentbiketime = accbiketime;
        double margprob = 1;
        double thisprob = returnprob;
        boolean end = false;
        double expectedcost = 0;
        double abandonprob = 0;
        Station current = sd.station;
        int neighbours = 0;
        while (!end) {
            double thisabsolutprob = margprob * thisprob;
            double walkdist = graphManager.estimateDistance(current.getPosition(), destination, "foot");
            double walktime = walkdist / expectedwalkingVelocity;
            double thiscost = costReturn(walktime, currentbiketime, thisprob);
            double newmargprob = margprob - thisabsolutprob;
            expectedcost = expectedcost + (margprob * thiscost);
            if (newmargprob <= minimumMarginProbability) {
                way.add(new stationPoint(current, accbiketime, 0, margprob));
                end = true;
            } else {
                way.add(new stationPoint(current, accbiketime, 0, thisabsolutprob));
                //find best neighbour
                lookedlist.add(current);
                Station closestneighbour = bestNeighbourReturn(thisprob, current, lookedlist, allstats, destination, accbikedist, probtimeoffset);
                if (closestneighbour != null && neighbours<maxneighbours) {
                    double newdist = graphManager.estimateDistance(current.getPosition(), closestneighbour.getPosition(), "bike");
                    accbikedist = accbikedist + newdist;
                    accbiketime = accbikedist / expectedcyclingVelocity;
                    thisprob = probutils.calculateReturnProbability(closestneighbour, probtimeoffset + accbiketime);
                    if (setneihbour) {
                        sd.bestNeighbour = closestneighbour;
                        sd.bestNeighbourReturnWalktime = graphManager.estimateDistance(closestneighbour.getPosition(), destination, "foot") / expectedwalkingVelocity;
                        sd.bestNeighbourProbability = thisprob;
                        setneihbour = false;
                    }
                    current = closestneighbour;
                    currentbiketime = newdist / expectedwalkingVelocity;
                    margprob = newmargprob;
                    neighbours++;
                } else { //if no best neigbour found we assume a best neigbour with probability 1 at unsuccessCostReturn seconds
                    abandonprob = newmargprob;
                    if (thiscost>unsuccessCostReturn) expectedcost = expectedcost + (newmargprob * thiscost);
                    expectedcost = expectedcost + (newmargprob * unsuccessCostReturn);
                    end = true;
                }
            }
        }
        sd.abandonProbability = abandonprob;
        sd.expectedTimeIfNotAbandon = expectedcost;
        sd.totalCost = expectedcost;
        return sd.totalCost;
    }

    double calcCostRentcomp(double timecost, double prob) {
        double sp = Math.pow(prob, 1);
  //      if (timecost>=unsuccessCostRent)  return timecost;
        return timecost + (1 - sp) * unsuccessCostRent;
//        return time/probability;
    }

    double calcCostReturncomp(double walktime, double biketime, double prob) {
        double timecost = biketime + walktime;
        double sp = Math.pow(prob, 1);
        if (walktime>=unsuccessCostReturn)  return biketime + walktime;
        return biketime + sp * walktime + (1 - sp) * unsuccessCostReturn;
//        return time/probability;
    }

    //with probabilities recalculated at the correct time
    private Station bestNeighbourRent(double lastprob, Station s, List<Station> lookedlist, List<Station> allstats,
            double accwalkdistance, double probtimeoffset, double maxdistance) {
        double newbestValueFound = Double.MAX_VALUE;
        //       double currentcost=calcCostRentcomp((accwalkdistance) / expectedwalkingVelocity,lastprob);
        Station bestneighbour = null;
        for (Station nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double dist = s.getPosition().eucleadeanDistanceTo(nei.getPosition());
                if ((accwalkdistance + dist) <= maxdistance) {
                    dist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "foot");
                    if ((accwalkdistance + dist) <= maxdistance) {
                        double newacctime = (accwalkdistance + dist) / expectedwalkingVelocity;
                        double rentprob = probutils.calculateTakeProbability(nei, newacctime + probtimeoffset);
                        if (rentprob > 0 && rentprob > minProbSecondaryRecommendation) {
                            double thiscost = calcCostRentcomp(newacctime, rentprob);
                            //                      if (thiscost<currentcost) {
                            //calculate the cost of this potential neighbour
                            if (thiscost < newbestValueFound) {
                                newbestValueFound = thiscost;
                                bestneighbour = nei;
                            }
                            //                      }
                        }
                    }
                }
            }
        }
        return bestneighbour;
    }

    //with probs recalculated at the correct time
    private Station bestNeighbourReturn(double lastprob, Station s, List<Station> lookedlist, List<Station> allstats, GeoPoint destination,
            double accbikedistance, double probtimeoffset) {
        double newbestValueFound = Double.MAX_VALUE;
        double lastwalkdist = graphManager.estimateDistance(s.getPosition(), destination, "foot");
        double lastwalktime = lastwalkdist / expectedwalkingVelocity;

        Station bestneighbour = null;
        for (Station nei : allstats) {
            if (!lookedlist.contains(nei)) {
                double dist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "bike");
                double altthisbiketime = (accbikedistance + dist) / expectedcyclingVelocity;
                double returnprob = probutils.calculateReturnProbability(nei, altthisbiketime + probtimeoffset);
                if (returnprob > 0 && returnprob > minProbSecondaryRecommendation) {
                    dist = graphManager.estimateDistance(nei.getPosition(), destination, "foot");
                    double altthiswalktime = dist / expectedwalkingVelocity;
                    double thiscost = calcCostReturncomp(altthiswalktime, altthisbiketime, returnprob);
                    //                if (thiscost<currentcost) {
                    if (thiscost < newbestValueFound) {
                        newbestValueFound = thiscost;
                        bestneighbour = nei;
                        //                    }
                    }
                }
            }
        }
        return bestneighbour;
    }

    class stationPoint {

        Station station;
        double offsettimereached;
        double takeprob;
        double returnprob;

        stationPoint(Station sd, double t, double tprob, double rprob) {
            this.station = sd;
            offsettimereached = t;
            takeprob = tprob;
            returnprob = rprob;
        }
    }
}
