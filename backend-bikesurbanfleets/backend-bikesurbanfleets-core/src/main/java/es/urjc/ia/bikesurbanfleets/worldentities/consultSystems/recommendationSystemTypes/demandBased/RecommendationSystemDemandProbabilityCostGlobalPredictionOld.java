package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.demandBased;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.SellamDistribution;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_COST_PREDICTION_old")
public class RecommendationSystemDemandProbabilityCostGlobalPredictionOld extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12 / 2;//2.25D; //with 3 the time is quite worse
        private double cyclingVelocity = 6.0 / 2;//2.25D; //reduciendo este factor mejora el tiempo, pero empeora los indicadores 
        private double walkingVelocityExpected = 1.12 / 2D;//2.25D; //with 3 the time is quite worse
        private double cyclingVelocityExpected = 6.0 / 2D;//2.25D; //reduciendo este factor mejora el tiempo, pero empeora los indicadores 
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.999;

        private double probabilityUsersObey = 1;
        private double factor = 1D / (double) (1000);
        private double penalisationfactor = 3;
        private double bikefactor = 0.1D;
 
    }       
 

    boolean takeintoaccountexpected = true;
    boolean takeintoaccountcompromised = true;

    boolean printHints = true;
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityCostGlobalPredictionOld(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(ss);
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
        //       demandManager=infraestructureManager.getDemandManager();
        this.infrastructureManager.POBABILITY_USERSOBEY = this.parameters.probabilityUsersObey;
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint currentposition) {
        List<Recommendation> result;
        List<Station> stations = infrastructureManager.consultStations().stream().
                sorted(byDistance(currentposition)).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationCostRent(stations, currentposition);
            if (printHints) {
                printRecomendations(su, true);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbability());
                return r;
            }
            ).collect(Collectors.toList());
            //add values to the expeted takes
            StationUtilityData first = su.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            this.infrastructureManager.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / this.parameters.walkingVelocityExpected), true);
        } else {
            result = new ArrayList<>();
            System.out.println("no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    private void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
            int max = Math.min(10, su.size());
            System.out.println();
            if (take) {
                System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime());
                probst += su.get(0).getProbability();
                callst++;
                System.out.format("Expected successrate take: %9.8f %n", (probst / callst));
            } else {
                System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime());
                probsr += su.get(0).getProbability();
                callsr++;
                System.out.format("Expected successrate return: %9.8f %n", (probsr / callsr));
            }
            if (su.get(0).getProbability() < 0.6) {
                System.out.format("LOW PROB %9.8f %n", su.get(0).getProbability());
                lowprobs++;
            }
            if (take) {
                System.out.println("         id av ca   wdist  prob       cl   cwdist cprob        util");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("Station %3d %2d %2d %7.1f %9.8f %3d %7.1f %9.8f %9.2f %n",
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.walkdist,
                            s.getProbability(),
                            s.closest,
                            s.closestwalkdist,
                            s.closestprob,
                            s.getCost());
                }
            } else {
                System.out.println("         id av ca   wdist  bdist    prob      clo  cwdis  cbdis   cprob      util");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("Station %3d %2d %2d %7.1f %7.1f %9.8f %3d %7.1f %7.1f %9.8f %9.2f %n",
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.walkdist,
                            s.bikedist,
                            s.getProbability(),
                            s.closest,
                            s.closestwalkdist,
                            s.closestbikedist,
                            s.closestprob,
                            s.getCost());
                }
            }
        }
    }

    private int lowprobs = 0;
    private double probsr = 0D;
    private int callsr = 0;
    private double probst = 0D;
    private int callst = 0;

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = infrastructureManager.consultStations().stream().
                sorted(byDistance(destination)).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationCostReturn(stations, destination, currentposition);
            if (printHints) {
                printRecomendations(su, false);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbability());
                return r;
            }
            ).collect(Collectors.toList());
            //add values to the expeted returns
            StationUtilityData first = su.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            this.infrastructureManager.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / this.parameters.cyclingVelocityExpected), false);
        } else {
            System.out.println("no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<StationUtilityData> getStationCostRent(List<Station> stations, GeoPoint currentposition) {
        //calcultae the probabilities of all stations
        List<StationUtilityData> temp = new ArrayList<>();
        List<StationUtilityData> res = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double dist = currentposition.distanceTo(s.getPosition());
            int offtime = (int) (dist / this.parameters.walkingVelocity);
            double prob = infrastructureManager.getAvailableBikeProbability(s, offtime,
                    takeintoaccountexpected, takeintoaccountcompromised);
            sd.setProbability(prob).setTime(offtime).setDistance(dist);
            sd.walkdist=dist;
            temp.add(sd);
        }
        //now calculate the costs
        for (StationUtilityData sd : temp) {
            List<StationUtilityData> lookedlist = new ArrayList<>();
            double cost = calculateCostRent(sd, 1, sd.getDistance(), lookedlist, temp, true);
            sd.setCost(cost);
            addRent(sd, res);
        }
        return res;
    }

    public List<StationUtilityData> getStationCostReturn(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        List<StationUtilityData> res = new ArrayList<>();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double dist = currentposition.distanceTo(s.getPosition());
            int offtime = (int) (dist / this.parameters.cyclingVelocity);
            double prob = infrastructureManager.getAvailableSlotProbability(s, offtime,
                    takeintoaccountexpected, takeintoaccountcompromised);
            int time = (int) ((dist / this.parameters.cyclingVelocity)
                    + (s.getPosition().distanceTo(destination) / this.parameters.walkingVelocity));
            sd.setProbability(prob).setDistance(dist).setTime(time);
            sd.walkdist=s.getPosition().distanceTo(destination);
            sd.bikedist=dist* this.parameters.bikefactor;
            temp.add(sd);
        }
        //now calculate the costs
        for (StationUtilityData sd : temp) {
            List<StationUtilityData> lookedlist = new ArrayList<>();
            double cost = calculateCostReturn(sd, 1, sd.bikedist, destination, lookedlist, temp, true);
            sd.setCost(cost);
            addReturn(sd, res);
        }
        return res;
    }

    private double calculateCostRent(StationUtilityData sd,
            double margprob, double currentdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double accost = margprob * currentdist;
        if (margprob <= (1-this.parameters.desireableProbability)) {
            return accost;
        }
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd, lookedlist, allstats);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
        double newmargprob = margprob * (1 - sd.getProbability());
        if (start) {
            sd.closest = closestneighbour.getStation().getId();
            sd.closestwalkdist = newdist;
            sd.closestprob = closestneighbour.getProbability();
        }
        double margcost = this.parameters.penalisationfactor *calculateCostRent(closestneighbour, newmargprob, newdist, lookedlist, allstats, false);
        return margcost + accost;
    }

    private double calculateCostRent_best(StationUtilityData sd,
            double margprob, double costgettingtosd, 
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        //end condition
        if (margprob <= (1-this.parameters.desireableProbability)) {
            return costgettingtosd;
        }
        //find best neighbour
        double prob=sd.getProbability();
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourRent(sd, lookedlist, allstats);
        double newdist=sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition());
        if (start) {
            sd.closest = closestneighbour.getStation().getId();
            sd.closestwalkdist = newdist;
            sd.closestprob = closestneighbour.getProbability();
        }
        double newmargprob = margprob * (1 - prob);
        double newcostgettingtosd=costgettingtosd+newdist;
//        double margcost = this.parameters.penalisationfactor * calculateCostRent_best(closestneighbour, newmargprob, newcostgettingtosd, lookedlist, allstats, false);
        double margcost =  this.parameters.penalisationfactor * calculateCostRent_best(closestneighbour, newmargprob, newcostgettingtosd, lookedlist, allstats, false);
        return prob*costgettingtosd + (1-prob)*margcost;
    }

  private double calculateCostReturn_best(StationUtilityData sd,
            double margprob, double costgettingtosd, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        //end condition
        if (margprob <= (1-this.parameters.desireableProbability)) {
            return costgettingtosd;
        }
        //find best neighbour
        double prob=sd.getProbability();
        double walkdist= sd.getStation().getPosition().distanceTo(destination);
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd, lookedlist, allstats, destination);
        double newdist=sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())* this.parameters.bikefactor;
        if (start) {
            sd.closest = closestneighbour.getStation().getId();
            sd.closestbikedist = newdist;
            sd.closestprob = closestneighbour.getProbability();
            sd.closestwalkdist = closestneighbour.getStation().getPosition().distanceTo(destination);
            sd.closestprob = closestneighbour.getProbability();
        }
        double newmargprob = margprob * (1 - prob);
        double newcostgettingtosd=costgettingtosd+newdist;
        double margcost = this.parameters.penalisationfactor * calculateCostReturn_best(closestneighbour, newmargprob, newcostgettingtosd, destination, lookedlist, allstats, false);
        return prob*(costgettingtosd+ walkdist) + (1-prob)*margcost;
    }
    private double calculateCostReturn(StationUtilityData sd,
            double margprob, double currentdist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double walkcost =  margprob * sd.getStation().getPosition().distanceTo(destination);
        double bikecost =  margprob * currentdist;
        if (margprob <= (1-this.parameters.desireableProbability)) {
            return bikecost + walkcost;
        }
        walkcost = walkcost * sd.getProbability();
        double acccost = bikecost + walkcost;
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd, lookedlist, allstats,destination);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())* this.parameters.bikefactor;
        double newmargprob = margprob * (1 - sd.getProbability());
        if (start) {
            sd.closest = closestneighbour.getStation().getId();
            sd.closestbikedist = newdist;
            sd.closestprob = closestneighbour.getProbability();
            sd.closestwalkdist = closestneighbour.getStation().getPosition().distanceTo(destination);
        }
        double margcost = this.parameters.penalisationfactor * calculateCostReturn(closestneighbour, newmargprob, newdist, destination, lookedlist, allstats, false);
        return margcost + acccost;
    }

    private StationUtilityData bestNeighbourRent(StationUtilityData sd, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats) {
        StationUtilityData closest = null;
        double clostesutil = Double.MAX_VALUE;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)&& nei.getProbability()>=0.6) {
                double dist = nei.getStation().getPosition().distanceTo(sd.getStation().getPosition());
                double util=dist*nei.getProbability()+2000*(1-nei.getProbability());
                if (util < clostesutil) {
                    clostesutil = util;
                    closest = nei;
                    
                }
            }
        }
        return closest;
    }
    private StationUtilityData bestNeighbourReturn(StationUtilityData sd, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats, GeoPoint destination) {
        StationUtilityData closest = null;
        double clostesutil = Double.MAX_VALUE;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei)&& nei.getProbability()>=0.6) {
                double bdist = nei.getStation().getPosition().distanceTo(sd.getStation().getPosition());
                double wdist=nei.getStation().getPosition().distanceTo(destination);
                double util=(bdist* this.parameters.bikefactor+wdist)
                        *nei.getProbability()+1000*(1-nei.getProbability());
                if (util < clostesutil) {
                    clostesutil = util;
                    closest = nei;
                }
            }
        }
        return closest;
    }
    private StationUtilityData getClosestNeighbour(StationUtilityData sd, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats) {
        StationUtilityData closest = null;
        double clostesdist = Double.MAX_VALUE;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbability()>=0.6
                    ) {
                double dist = nei.getStation().getPosition().distanceTo(sd.getStation().getPosition());
                if (dist < clostesdist) {
                    clostesdist = dist;
                    closest = nei;
                }
            }
        }
        return closest;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            if (newSD.getCost() < oldSD.getCost()) {
                return true;
            } else {
                return false;
            }
        }
        if (oldSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getCost() < oldSD.getCost()) {
            return true;
        } else {
            return false;
        }
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getCost() < oldSD.getCost()) {
            return true;
        }
        return false;
    }

    private void addRent(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameRent(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }

    private void addReturn(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameReturn(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }

    private static Comparator<Station> byDistance(GeoPoint point) {
        return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
    }


    public double getGlobalCostImprovementIfTake(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {

        //first calculate the difference in the probabilities of gettinga a bike or slot if the bike is taken at the station
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            InfrastructureManager.ExpBikeChangeResult er = infrastructureManager.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * infrastructureManager.POBABILITY_USERSOBEY);
            estimatedslots -= (int) Math.floor(er.changes * infrastructureManager.POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * infrastructureManager.POBABILITY_USERSOBEY);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * infrastructureManager.POBABILITY_USERSOBEY);
                //            }
            }
        }
        double takedemandattimeoffset = (infrastructureManager.getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (infrastructureManager.getCurrentSlotDemand(s) * timeoffset) / 3600D;
        //probability that a bike exists 
        int k = 1 - estimatedbikes;
        double probbikediff = -SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        k = 1 - estimatedslots - 1;
        double probslotdiff = SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        //now calculate the demands at the future point relative to the global demand
        double futtakedemand = infrastructureManager.getFutureBikeDemand(s, (int) timeoffset);
        double futreturndemand = infrastructureManager.getFutureSlotDemand(s, (int) timeoffset);
        double futglobaltakedem = infrastructureManager.getFutureGlobalBikeDemand((int) timeoffset);
        double futglobalretdem = infrastructureManager.getFutureGlobalSlotDemand((int) timeoffset);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * probbikediff
                + (futreturndemand / futglobalretdem) * probslotdiff;
        return relativeimprovemente;
    }

    public double getGlobalCostImprovementIfReturn(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {

        //first calculate the difference in the probabilities of gettinga a bike or slot if the bike is taken at the station
        int estimatedbikes = s.availableBikes();
        int estimatedslots = s.availableSlots();
        if (takeintoaccountexpected) {
            InfrastructureManager.ExpBikeChangeResult er = infrastructureManager.getExpectedBikechanges(s.getId(), timeoffset);
            estimatedbikes += (int) Math.floor(er.changes * infrastructureManager.POBABILITY_USERSOBEY);
            estimatedslots -= (int) Math.floor(er.changes * infrastructureManager.POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
                //            if ((estimatedbikes+minpostchanges)<=0){
                estimatedbikes += (int) Math.floor(er.minpostchanges * infrastructureManager.POBABILITY_USERSOBEY);
                estimatedslots -= (int) Math.floor(er.maxpostchanges * infrastructureManager.POBABILITY_USERSOBEY);
                //            }
            }
        }
        double takedemandattimeoffset = (infrastructureManager.getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (infrastructureManager.getCurrentSlotDemand(s) * timeoffset) / 3600D;
        //probability that a bike exists 
        int k = 1 - estimatedbikes - 1;
        double probbikediff = -SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        k = 1 - estimatedslots;
        double probslotdiff = SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        //now calculate the demands at the future point relative to the global demand
        double futtakedemand = infrastructureManager.getFutureBikeDemand(s, (int) timeoffset);
        double futreturndemand = infrastructureManager.getFutureSlotDemand(s, (int) timeoffset);
        double futglobaltakedem = infrastructureManager.getFutureGlobalBikeDemand((int) timeoffset);
        double futglobalretdem = infrastructureManager.getFutureGlobalSlotDemand((int) timeoffset);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * probbikediff
                + (futreturndemand / futglobalretdem) * probslotdiff;
        return relativeimprovemente;
    }

}
