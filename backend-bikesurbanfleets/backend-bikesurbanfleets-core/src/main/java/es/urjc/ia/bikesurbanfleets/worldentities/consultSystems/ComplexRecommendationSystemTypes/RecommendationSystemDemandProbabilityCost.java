package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_cost")
public class RecommendationSystemDemandProbabilityCost extends RecommendationSystemDemandProbabilityBased {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;

        private double minimumMarginProbability = 0.001;
        private double minProbSecondRecommendation = 0.5;
        private double minProbFirstRecommendation = 0;
        private double penalisationfactorrent = 1;
        private double penalisationfactorreturn = 1;
        private double bikefactor = 0.1D;
        private double MaxCostValue = 100000;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRent = 3000;
        private double unsucesscostReturn = 2000;

    }

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(recomenderdef,ss);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i=0;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake()> 0) {
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double cost = calculateCostRentHeuristic(sd, 1, sd.getWalkdist(), lookedlist, stationdata, true);
                sd.setTotalCost(cost);
                addRent(sd, orderedlist);
                i++;
            }
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i=0;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityReturn()> 0) {
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double ajdist=sd.getBikedist()*this.parameters.bikefactor;
                double cost = calculateCostReturnHeuristic(sd, 1, ajdist, userdestination, lookedlist, stationdata, true);
                sd.setTotalCost(cost);
                addRent(sd, orderedlist);
                i++;
            }
        }
        return orderedlist;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostRent_best(StationUtilityData sd,
            double margprob, double accdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double prob = sd.getProbabilityTake();
        double newmargprob = margprob * (1 - prob);
        if (margprob <= this.parameters.minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= this.parameters.minimumMarginProbability) {
            return (1 - this.parameters.minimumMarginProbability / margprob) * accdist;
        }
        //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), newmargprob, lookedlist, allstats);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
        double newaccdist = accdist + newdist;
        if (start) {
            sd.bestNeighbour=closestneighbour;
        }
        double margcost = calculateCostRent_best(closestneighbour, newmargprob, newaccdist, lookedlist, allstats, false);
        return prob * accdist
                + this.parameters.penalisationfactorrent * (1 - prob) * (margcost + this.parameters.unsucesscostRent);
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostReturn_best(StationUtilityData sd,
            double margprob, double accbikedist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double prob = sd.getProbabilityReturn();
        double newmargprob = margprob * (1 - prob);
        double walkdist = sd.getStation().getPosition().distanceTo(destination);
        if (margprob <= this.parameters.minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= this.parameters.minimumMarginProbability) {
            return (1 - this.parameters.minimumMarginProbability / margprob) * accbikedist
                    + walkdist * (margprob - this.parameters.minimumMarginProbability) / margprob;
        }
        //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), newmargprob, lookedlist, allstats, destination);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) * this.parameters.bikefactor;
        if (start) {
            sd.bestNeighbour = closestneighbour;
        }
        double newaccbikedist = accbikedist + newdist;
        double margcost = calculateCostReturn_best(closestneighbour, newmargprob, newaccbikedist, destination, lookedlist, allstats, false);
        return prob * (accbikedist + walkdist)
                + this.parameters.penalisationfactorreturn * (1 - prob) * (margcost + this.parameters.unsucesscostReturn);
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostRentHeuristic(StationUtilityData sd,
            double margprob, double currentdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double thiscost = (margprob - this.parameters.minimumMarginProbability) * currentdist;
        double newmargprob = margprob * (1 - sd.getProbabilityTake());
        if (margprob <= this.parameters.minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= this.parameters.minimumMarginProbability) {
            return thiscost;
        }
        //find best neighbour
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd.getStation(), newmargprob, lookedlist, allstats);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
        if (start) {
            sd.bestNeighbour = closestneighbour;
        }
        double margcost = newmargprob * this.parameters.unsucesscostRent + calculateCostRentHeuristic(closestneighbour, newmargprob, newdist, lookedlist, allstats, false);
        return thiscost + this.parameters.penalisationfactorrent * margcost;
    }

    //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostReturnHeuristic(StationUtilityData sd,
            double margprob, double currentdist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double thisbikecost = (margprob - this.parameters.minimumMarginProbability) * currentdist;
        double thiswalkdist = sd.getStation().getPosition().distanceTo(destination);
        double newmargprob = margprob * (1 - sd.getProbabilityReturn());
        double thistotalcost = thisbikecost;
        if (margprob <= this.parameters.minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= this.parameters.minimumMarginProbability) {
            thistotalcost = thistotalcost + thiswalkdist * (margprob - this.parameters.minimumMarginProbability);
            return thistotalcost;
        } else {
            thistotalcost = thistotalcost + thiswalkdist * margprob * sd.getProbabilityReturn();
        }
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), newmargprob, lookedlist, allstats, destination);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) * this.parameters.bikefactor;
        if (start) {
            sd.bestNeighbour=closestneighbour;
        }
        double margvalue = newmargprob * this.parameters.unsucesscostReturn + calculateCostReturnHeuristic(closestneighbour, newmargprob, newdist, destination, lookedlist, allstats, false);
        return thistotalcost + this.parameters.penalisationfactorreturn * margvalue;
    }

    private StationUtilityData bestNeighbourRent(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats) {
        double newbestValueFound = Double.MAX_VALUE;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityTake()> this.parameters.minProbSecondRecommendation) {
                double newdist = s.getPosition().distanceTo(nei.getStation().getPosition());
                double altthiscost = (newmargprob - this.parameters.minimumMarginProbability) * newdist;
                double altnewmargprob = newmargprob * (1 - nei.getProbabilityTake());
                if (altnewmargprob <= this.parameters.minimumMarginProbability) {
                    altthiscost = altthiscost;
                } else {
                    altthiscost = altthiscost + (altnewmargprob - this.parameters.minimumMarginProbability) * this.parameters.MaxCostValue;
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
            if (!lookedlist.contains(nei) && nei.getProbabilityReturn() > this.parameters.minProbSecondRecommendation) {
                double altthisbikedist = s.getPosition().distanceTo(nei.getStation().getPosition()) * this.parameters.bikefactor;
                double altthisbikecost = (newmargprob - this.parameters.minimumMarginProbability) * altthisbikedist;
                double altthiswalkdist = nei.getStation().getPosition().distanceTo(destination);
                double altnewmargprob = newmargprob * (1 - nei.getProbabilityReturn());
                double alttotalcost = altthisbikecost;
                if (altnewmargprob <= this.parameters.minimumMarginProbability) {
                    alttotalcost = alttotalcost + altthiswalkdist * (newmargprob - this.parameters.minimumMarginProbability);
                } else {
                    alttotalcost = alttotalcost + altthiswalkdist * newmargprob * nei.getProbabilityReturn()
                            + (altnewmargprob - this.parameters.minimumMarginProbability) * this.parameters.MaxCostValue;
                }
                if (alttotalcost < newbestValueFound) {
                    newbestValueFound = alttotalcost;
                    bestneighbour = nei;
                }
            }
        }
        return bestneighbour;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getWalkdist()<= this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            if (newSD.getProbabilityTake()>= this.parameters.minProbFirstRecommendation
                    && oldSD.getProbabilityTake() >= this.parameters.minProbFirstRecommendation) {
                if (newSD.getTotalCost() < oldSD.getTotalCost()) {
                    return true;
                } else {
                    return false;
                }
            }
            if (newSD.getProbabilityTake() >= this.parameters.minProbFirstRecommendation) {
                return true;
            }
            if (oldSD.getProbabilityTake() >= this.parameters.minProbFirstRecommendation) {
                return false;
            }
            if (newSD.getTotalCost() < oldSD.getTotalCost()) {
                return true;
            } else {
                return false;
            }
        }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return true;
        }
        if (newSD.getTotalCost()< oldSD.getTotalCost()) {
            return true;
        } else {
            return false;
        }
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            if (newSD.getProbabilityReturn()>= this.parameters.minProbFirstRecommendation
                    && oldSD.getProbabilityReturn() >= this.parameters.minProbFirstRecommendation) {
                if (newSD.getTotalCost() < oldSD.getTotalCost()) {
                    return true;
                } else {
                    return false;
                }
            }
            if (newSD.getProbabilityReturn() >= this.parameters.minProbFirstRecommendation) {
                return true;
            }
            if (oldSD.getProbabilityReturn() >= this.parameters.minProbFirstRecommendation) {
                return false;
            }
            if (newSD.getTotalCost() < oldSD.getTotalCost()) {
                return true;
            } else {
                return false;
            }
        }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return true;
        }
        if (newSD.getTotalCost() < oldSD.getTotalCost()) {
            return true;
        } else {
            return false;
        }
    }

}
