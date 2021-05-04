/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.common.util.StationProbabilitiesQueueBased;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.UtilitiesProbabilityCalculator.ExpectedUnsuccessData;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.util.List;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex.ComplexCostCalculatorNew.stationPoint;
import java.util.ArrayList;

/**
 *
 * @author holger
 */
public class FutureCostCalculatorNew {

    //methods for cost calculations
    public FutureCostCalculatorNew(
            double unsucesscostRentPenalisation,
            double unsucesscostReturnPenalisation,
            UtilitiesProbabilityCalculator recutils,
            double walkvel, double cycvel, GraphManager gm) {
        this.probutils = (UtilitiesProbabilityCalculationQueue) recutils;
        unsucesscostRent = unsucesscostRentPenalisation;
        unsucesscostReturn = unsucesscostReturnPenalisation;
        graphManager = gm;
        expectedwalkingVelocity = walkvel;
        expectedcyclingVelocity = cycvel;
    }

    final double maxNeighbourrent = 500;
    final double maxNeighbourreturn = 500;
    final double expectedwalkingVelocity;
    final double expectedcyclingVelocity;
    final GraphManager graphManager;
    final double unsucesscostRent;
    final double unsucesscostReturn;
    UtilitiesProbabilityCalculationQueue probutils;

    class ddPair {

        public ddPair(double t, double r) {
            takecostdiff = t;
            returncostdiff = r;
        }

        public ddPair() {
        }
        double takecostdiff;
        double returncostdiff;
    }

    public ddPair calculateFurtureCostChangeTakeExpectedFails(Station s, double takeprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats) {
        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutRent(s, arrivaloffset, timeintervallforPrediction);
        double usuccessrentdiff = ed.expUnsuccessRentAfterTake - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterTake - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff < 0 || unsuccessreturndiff > 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }
        double extrarentcost = bestNeighbourRent(s, allstats, arrivaloffset + timeintervallforPrediction / 2d);
        double extrareturncost = bestNeighbourReturn(s, allstats, arrivaloffset + timeintervallforPrediction / 2d);

        ddPair res = new ddPair();
        res.takecostdiff = usuccessrentdiff
                * // the expected fails 
                extrarentcost // the extra cost for fails depending on the next neighbour
                * takeprob;          //the probability that the take will take place;
        res.returncostdiff = unsuccessreturndiff
                * // the expected fails
                extrareturncost // the extra cost for fails depending on the next neighbour
                * takeprob;//the probability that the take will take place;
        return res;
    }

    public ddPair calculateFurtureCostChangeTakeExpectedFails2(Station s, double takeprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats) {
        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutRent(s, arrivaloffset, timeintervallforPrediction);
        double usuccessrentdiff = ed.expUnsuccessRentAfterTake - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterTake - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff < 0 || unsuccessreturndiff > 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }

        ddPair res=getSurroundingCostRent(s,(arrivaloffset + timeintervallforPrediction / 2d),
            allstats,usuccessrentdiff, 
            (int)Math.floor(ed.expUnsuccessRentIfNoChange),
            unsuccessreturndiff,
            (int)Math.floor(ed.expUnsuccessReturnIfNoChange),
            true);

        res.takecostdiff = res.takecostdiff
                * takeprob;          //the probability that the take will take place;
        res.returncostdiff = res.returncostdiff
                * takeprob;//the probability that the take will take place;
        return res;
    }

    public ddPair calculateFurtureCostChangeReturnExpectedFails2(Station s, double returnprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats) {
        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutReturn(s, arrivaloffset, timeintervallforPrediction);
        double usuccessrentdiff = ed.expUnsuccessRentAfterReturn - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterReturn - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff > 0 || unsuccessreturndiff < 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }
        ddPair res=getSurroundingCostRent(s,(arrivaloffset + timeintervallforPrediction / 2d),
            allstats,usuccessrentdiff, 
            (int)Math.floor(ed.expUnsuccessRentIfNoChange),
            unsuccessreturndiff,
            (int)Math.floor(ed.expUnsuccessReturnIfNoChange),
            true);

        res.takecostdiff = res.takecostdiff
                * returnprob;          //the probability that the take will take place;
        res.returncostdiff = res.returncostdiff
                * returnprob;//the probability that the take will take place;
        return res;
    }

    public ddPair calculateFurtureCostChangeReturnExpectedFails(Station s, double returnprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats) {
        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutReturn(s, arrivaloffset, timeintervallforPrediction);
        double usuccessrentdiff = ed.expUnsuccessRentAfterReturn - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterReturn - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff > 0 || unsuccessreturndiff < 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }
        double extrarentcost = bestNeighbourRent(s, allstats, arrivaloffset + timeintervallforPrediction / 2d);
        double extrareturncost = bestNeighbourReturn(s, allstats, arrivaloffset + timeintervallforPrediction / 2d);

        ddPair res = new ddPair();
        res.takecostdiff = usuccessrentdiff * extrarentcost * returnprob;//* unsucesscostRent;
        res.returncostdiff = unsuccessreturndiff * extrareturncost * returnprob;//unsucesscostReturn ;
        return res;
    }

    public ddPair calculateFurtureCostChangeTakeExpectedFailsWay(List<stationPoint> way, double timeintervallforPrediction, List<Station> allstats) {
        double acctakecost = 0;
        double accreturncost = 0;
        for (stationPoint wp : way) {
            ddPair r = calculateFurtureCostChangeTakeExpectedFails(wp.station, wp.takeprob, wp.offsettimereached, timeintervallforPrediction, allstats);
            acctakecost += r.takecostdiff;
            accreturncost += r.returncostdiff;
        }
        ddPair res = new ddPair();
        res.takecostdiff = acctakecost;
        res.returncostdiff = accreturncost;
        return res;
    }

    public ddPair calculateFurtureCostChangeReturnExpectedFailsWay(List<stationPoint> way, double timeintervallforPrediction, List<Station> allstats) {
        double acctakecost = 0;
        double accreturncost = 0;
        for (stationPoint wp : way) {
            ddPair r = calculateFurtureCostChangeReturnExpectedFails(wp.station, wp.returnprob, wp.offsettimereached, timeintervallforPrediction, allstats);
            acctakecost += r.takecostdiff;
            accreturncost += r.returncostdiff;
        }
        ddPair res = new ddPair();
        res.takecostdiff = acctakecost;
        res.returncostdiff = accreturncost;
        return res;
    }

    public ddPair calculateFurtureCostChangeTakeExpectedFailsSurrounding(
            Station s, double takeprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats,
            double maxdistancesurrounding) {

        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutRentSurrounding(s, arrivaloffset, timeintervallforPrediction, maxdistancesurrounding, allstats);
        double usuccessrentdiff = ed.expUnsuccessRentAfterTake - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterTake - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff < 0 || unsuccessreturndiff > 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }

        ddPair res = new ddPair();
        res.takecostdiff = usuccessrentdiff
                * // the expected fails 
                unsucesscostRent // the extra cost for fails depending on the next neighbour
                * takeprob;          //the probability that the take will take place;
        res.returncostdiff = unsuccessreturndiff
                * // the expected fails
                unsucesscostReturn // the extra cost for fails depending on the next neighbour
                * takeprob;//the probability that the take will take place;
        return res;
    }

    public ddPair calculateFurtureCostChangeReturnExpectedFailsSurrounding(
            Station s, double returnprob, double arrivaloffset, double timeintervallforPrediction, List<Station> allstats,
            double maxdistancesurrounding) {

        ExpectedUnsuccessData ed = probutils.calculateExpectedFutureFailsWithAndWithoutReturnSurrounding(s, arrivaloffset, timeintervallforPrediction, maxdistancesurrounding, allstats);
        double usuccessrentdiff = ed.expUnsuccessRentAfterReturn - ed.expUnsuccessRentIfNoChange;
        double unsuccessreturndiff = ed.expUnsuccessReturnAfterReturn - ed.expUnsuccessReturnIfNoChange;
        if (usuccessrentdiff > 0 || unsuccessreturndiff < 0) {
            System.out.println("EEEEERRRRROOOOORRRR: invalid cost station " + s.getId() + " " + usuccessrentdiff + " " + unsuccessreturndiff);
            throw new RuntimeException("invalid state");
        }
        ddPair res = new ddPair();
        res.takecostdiff = usuccessrentdiff * unsucesscostRent * returnprob;//* unsucesscostRent;
        res.returncostdiff = unsuccessreturndiff * unsucesscostReturn * returnprob;//unsucesscostReturn ;
        return res;
    }

    //with probabilities recalculated at the correct time
    private double bestNeighbourRent(Station s, List<Station> allstats,
            double probtimeoffset) {
        double newbestValueFound = unsucesscostRent;
        for (Station nei : allstats) {
            double dist = s.getPosition().eucleadeanDistanceTo(nei.getPosition());
            if (dist <= maxNeighbourrent && nei.getId() != s.getId()) {
                dist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "foot");
                if (dist <= maxNeighbourrent) {
                    double newacctime = dist / expectedwalkingVelocity;
                    double rentprob = probutils.calculateTakeProbability(nei, newacctime + probtimeoffset);
                    double thiscost;
                //    if (newacctime>unsucesscostRent) thiscost= newacctime;
                    thiscost= newacctime + (1 - rentprob) * unsucesscostRent;
                    if (thiscost < newbestValueFound) {
                        newbestValueFound = thiscost;
                    }

                }
            }
        }
        return newbestValueFound;
    }

    private double bestNeighbourReturn(Station s, List<Station> allstats,
            double probtimeoffset) {
        double newbestValueFound = unsucesscostReturn;
        for (Station nei : allstats) {
            double dist = s.getPosition().eucleadeanDistanceTo(nei.getPosition());
            if (dist <= maxNeighbourreturn && nei.getId() != s.getId()) {
                dist = graphManager.estimateDistance(s.getPosition(), nei.getPosition(), "bike");
                if (dist <= maxNeighbourreturn) {
                    double newbiketime = dist / expectedcyclingVelocity;
                    double newwalktime= dist / expectedwalkingVelocity ;
                    double returnprob = probutils.calculateReturnProbability(nei, newbiketime + probtimeoffset);
                    double thiscost;
                    if (newwalktime>unsucesscostReturn) thiscost= newbiketime+newwalktime;
                    thiscost= newbiketime + returnprob * newwalktime+ (1 - returnprob) * unsucesscostReturn;
                    if (thiscost < newbestValueFound) {
                        newbestValueFound = thiscost;
                    }
                }
            }
        }
        return newbestValueFound;
    }

    private ddPair getSurroundingCostRent(Station station,
            double time, List<Station> stations,
            double expectedrentfaildifference,
            int expectedrentfailsInt,
            double expectedreturnfaildifference,
            int expectedreturnfailsInt,
            boolean rent) {
        int suravcap = 0;
        int suravbikes = 0;
        double surtakedemandrate = 0;
        double surreturndemandrate = 0;
        int num = 0;
        double avdist=0;
        for (Station s : stations) {
            if (s.getId() != station.getId()) {
                double dist = station.getPosition().eucleadeanDistanceTo(s.getPosition());
                if ((dist <= maxNeighbourrent && rent) || (dist <= maxNeighbourreturn && !rent)) {
                    UtilitiesProbabilityCalculationQueue.IntTuple t = probutils.getAvailableCapandBikes(s, time);
                    suravcap += t.avcap;
                    suravbikes += t.avbikes;
                    surtakedemandrate += t.takedemandrate;
                    surreturndemandrate += t.returndemandrate;
                    num++;
                    avdist+=dist;
                }
            }
        }
        if (num==0) {
            return new ddPair(expectedrentfaildifference * unsucesscostRent, expectedreturnfaildifference * unsucesscostReturn);
        } else {
            avdist=avdist/((double)num);
            double rentcost = 0;
            double returncost = 0;
            int initialbikes = suravbikes;
            initialbikes = Math.max(Math.min(initialbikes, suravcap), 0);
            StationProbabilitiesQueueBased pc = new StationProbabilitiesQueueBased(
                    StationProbabilitiesQueueBased.Type.RungeKutta, UtilitiesProbabilityCalculationQueue.h, surreturndemandrate,
                    surtakedemandrate, suravcap, initialbikes);
            double[] p = pc.getProbabilityDistribution();
            int i = 0;
            double sum = 0;
            while (i < expectedrentfailsInt) {
                sum += p[i];
                i++;
            }
            double tcost=avdist/expectedwalkingVelocity;
            if (tcost>unsucesscostRent) rentcost = expectedrentfaildifference * tcost;
            else rentcost = expectedrentfaildifference * (sum * unsucesscostRent + (1 - sum) * tcost);
            //returncost
            i = p.length - 1;
            sum = 0;
            while (i >= expectedreturnfailsInt) {
                sum += p[i];
                i--;
            }
            double rcost=avdist/expectedcyclingVelocity+avdist/(expectedwalkingVelocity);
            if (rcost>unsucesscostReturn) returncost = expectedrentfaildifference * rcost;
            returncost = expectedreturnfaildifference * (sum * unsucesscostReturn + (1 - sum) * rcost);
            return new ddPair(rentcost, returncost);
        }
    }
}
