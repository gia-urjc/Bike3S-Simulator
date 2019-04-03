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
@RecommendationSystemType("DEMAND_cost_prediction")
public class RecommendationSystemDemandProbabilityCostGlobalPrediction extends RecommendationSystem {

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

        private double minimumMarginProbability = 0.001;
        private double minProbRecommendation = 0.5;
        private double probabilityUsersObey = 1;
        private double penalisationfactorrent = 1;
        private double penalisationfactorreturn = 1;
        private double bikefactor = 0.1D;
        private double MaxCostValue = Double.MAX_VALUE / 2D;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRent = 3000;
        private double unsucesscostReturn = 2000;

    }

    boolean takeintoaccountexpected = true;
    boolean takeintoaccountcompromised = true;

    boolean printHints = true;
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityCostGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
                System.out.println("         id av ca   wdist  prob       cl   cwdist cprob        cost     indcost    tcostdif     rcostdif");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("Station %3d %2d %2d %7.1f %9.8f %3d %7.1f %9.8f %9.2f %9.2f %9.2f %9.2f%n",
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.walkdist,
                            s.getProbability(),
                            s.closest,
                            s.closestwalkdist,
                            s.closestprob,
                            s.getTotalCost(),
                            s.getCost(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff());
                }
            } else {
                System.out.println("         id av ca   wdist  bdist    prob      clo  cwdis  cbdis   cprob        cost     indcost    tcostdif     rcostdif");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("Station %3d %2d %2d %7.1f %7.1f %9.8f %3d %7.1f %7.1f %9.8f %9.2f %9.2f %9.2f %9.2f%n",
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
                            s.getTotalCost(),
                            s.getCost(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff());
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
            calculateProbabilitiesTake(sd, offtime);
            sd.setTime(offtime).setDistance(dist);
            sd.walkdist = dist;
            temp.add(sd);
        }
        //now calculate the costs
        int i = 0;
        for (StationUtilityData sd : temp) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbability() > 0) {
                double cost=calculateCostsRentAtStation(sd,temp);
                sd.setTotalCost(cost);
                addRent(sd, res);
                i++;
            }
        }
        if (res.size() == 0) {
            System.out.println("ERROR take: no recommendation found with minimal parameters");
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
            calculateProbabilitiesReturn(sd, offtime);
            int time = (int) ((dist / this.parameters.cyclingVelocity)
                    + (s.getPosition().distanceTo(destination) / this.parameters.walkingVelocity));
            sd.setDistance(dist).setTime(time);
            sd.walkdist = s.getPosition().distanceTo(destination);
            sd.bikedist = dist * this.parameters.bikefactor;
            temp.add(sd);
        }
        //now calculate the costs
        int i = 0;
        for (StationUtilityData sd : temp) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbability() > 0) {
                double cost=calculateCostsReturnAtStation(sd,destination,temp);
                sd.setTotalCost(cost);
                addReturn(sd, res);
                i++;
            }
        }
        if (res.size() == 0) {
            System.out.println("ERROR return: no recommendation found with minimal parameters");
        }
        return res;
    }

    private void calculateProbabilitiesTake(StationUtilityData sd, double timeoffset) {
        Station s = sd.getStation();
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

        //probability that a bike exists and that is exists after taking one 
        int k = 1 - estimatedbikes;
        double probbike = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        double probbikeafter = probbike-SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);

        //probability that a slot exists and that is exists after taking one 
        k = 1 - estimatedslots;
        double probslot = SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);
        k = 1 - estimatedslots - 1;
        double probslotafter = probslot+ SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        sd.setProbabilityTake(probbike)
                .setProbability(probbike)
                .setProbabilityTakeAfter(probbikeafter)
                .setProbabilityReturn(probslot)
                .setProbabilityReturnAfter(probslotafter);
    }

    private void calculateProbabilitiesReturn(StationUtilityData sd, double timeoffset) {
        Station s = sd.getStation();
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

         //probability that a slot exists and that it exists after returning a bike 
        int k = 1 - estimatedslots;
        double probslot = SellamDistribution.calculateCDFSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);
        double probslotafter = probslot-SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);
 
        //probability that a bike exists and that is exists after retirning one 
        k = 1 - estimatedbikes;
        double probbike = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        k = 1 - estimatedbikes-1;
        double probbikeafter = probbike+SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);

        sd.setProbabilityTake(probbike)
                .setProbability(probslot)
                .setProbabilityTakeAfter(probbikeafter)
                .setProbabilityReturn(probslot)
                .setProbabilityReturnAfter(probslotafter);
    }

    private double calculateCostsRentAtStation(StationUtilityData sd, 
            List<StationUtilityData> allstats) {
        //takecosts
        List<StationUtilityData> lookedlist = new ArrayList<>();
        double costtake=calculateCostRentHeuristic(sd, sd.getProbabilityTake(), 1, sd.walkdist, lookedlist, allstats, true);

        //analyze global costs
        //takecost if bike is taken     
        lookedlist.clear();
        double costtakeafter=calculateCostRentHeuristic(sd, sd.getProbabilityTakeAfter(), 1, sd.walkdist, lookedlist, allstats, false);
        //return costs
        //take a close point to the station as hipotetical detsination
        GeoPoint hipodestination=sd.getStation().getPosition();
        lookedlist.clear();
        double costreturn=calculateCostReturnHeuristic(sd, sd.getProbabilityReturn(), 1, 50, hipodestination, lookedlist, allstats, false);
        lookedlist.clear();
        double costreturnafter=calculateCostReturnHeuristic(sd, sd.getProbabilityReturnAfter(), 1, 50, hipodestination, lookedlist, allstats, false);
        double difftakecost=costtakeafter-costtake;
        double diffretcost=costreturnafter-costreturn;
        //normalize with demand
        int timeoffset = (int) (sd.walkdist / this.parameters.walkingVelocity);
        double futtakedemand = infrastructureManager.getFutureBikeDemand(sd.getStation(), timeoffset);
        double futreturndemand = infrastructureManager.getFutureSlotDemand(sd.getStation(), timeoffset);
        double futglobaltakedem = infrastructureManager.getFutureGlobalBikeDemand(timeoffset);
        double futglobalretdem = infrastructureManager.getFutureGlobalSlotDemand(timeoffset);
        difftakecost=(futtakedemand/futglobaltakedem)*difftakecost;
        diffretcost=(futreturndemand/futglobalretdem)*diffretcost;

        double globalcost=costtake+difftakecost+diffretcost;
        sd.setCost(costtake).setTakecostdiff(difftakecost).setReturncostdiff(diffretcost);
        return globalcost;
    }

    private double calculateCostsReturnAtStation(StationUtilityData sd, GeoPoint destination,
            List<StationUtilityData> allstats) {
       //return costs
        //take a close point to the station as hipotetical detsination
        List<StationUtilityData> lookedlist = new ArrayList<>();
        double costreturn = calculateCostReturnHeuristic(sd, sd.getProbabilityReturn(), 1, sd.bikedist, destination, lookedlist, allstats, true);

        //analyze global costs
        //takecost if bike is taken   
        lookedlist.clear();
        double costtake=calculateCostRentHeuristic(sd, sd.getProbabilityTake(), 1, 100, lookedlist, allstats, false);
        lookedlist.clear();
        double costtakeafter=calculateCostRentHeuristic(sd, sd.getProbabilityTakeAfter(), 1, 100, lookedlist, allstats, false);

        //return costs
        //take a close point to the station as hipotetical detsination
        GeoPoint hipodestination=sd.getStation().getPosition();
        lookedlist.clear();
        double costreturnhip=calculateCostReturnHeuristic(sd, sd.getProbabilityReturn(), 1, 0, hipodestination, lookedlist, allstats, false);
        lookedlist.clear();
        double costreturnafterhip=calculateCostReturnHeuristic(sd, sd.getProbabilityReturnAfter(), 1, 0, hipodestination, lookedlist, allstats, false);
        double difftakecost=costtakeafter-costtake;
        double diffretcost=costreturnafterhip-costreturnhip;
        //noirmalize to demand
        int timeoffset = (int) ((sd.bikedist / this.parameters.bikefactor)/this.parameters.cyclingVelocity);
        double futtakedemand = infrastructureManager.getFutureBikeDemand(sd.getStation(), timeoffset);
        double futreturndemand = infrastructureManager.getFutureSlotDemand(sd.getStation(), timeoffset);
        double futglobaltakedem = infrastructureManager.getFutureGlobalBikeDemand(timeoffset);
        double futglobalretdem = infrastructureManager.getFutureGlobalSlotDemand(timeoffset);
        difftakecost=(futtakedemand/futglobaltakedem)*difftakecost;
        diffretcost=(futreturndemand/futglobalretdem)*diffretcost;

        double globalcost=costreturn+
                difftakecost+diffretcost;
        sd.setCost(costreturn).setTakecostdiff(difftakecost).setReturncostdiff(diffretcost);
        return globalcost;
    }

        //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostRentHeuristic(StationUtilityData sd, double sdprob,
            double margprob, double currentdist,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double thiscost = (margprob - this.parameters.minimumMarginProbability) * currentdist;
        double newmargprob = margprob * (1 - sdprob);
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
            sd.closest = closestneighbour.getStation().getId();
            sd.closestwalkdist = newdist;
            sd.closestprob = closestneighbour.getProbabilityTake();
        }
        double margcost = newmargprob * this.parameters.unsucesscostRent + 
                calculateCostRentHeuristic(closestneighbour, closestneighbour.getProbabilityTake(), newmargprob, newdist, lookedlist, allstats, false);
        return thiscost + this.parameters.penalisationfactorrent * margcost;
    }


    //DO NOT CHANGE IT IS WORKING :)
    private double calculateCostReturnHeuristic(StationUtilityData sd, double sdprob,
            double margprob, double currentdist, GeoPoint destination,
            List<StationUtilityData> lookedlist,
            List<StationUtilityData> allstats, boolean start) {
        double thisbikecost = (margprob - this.parameters.minimumMarginProbability) * currentdist;
        double thiswalkdist = sd.getStation().getPosition().distanceTo(destination);
        double newmargprob = margprob * (1 - sdprob);
        double thistotalcost = thisbikecost;
        if (margprob <= this.parameters.minimumMarginProbability) {
            throw new RuntimeException("error parameters");
        }
        if (newmargprob <= this.parameters.minimumMarginProbability) {
            thistotalcost = thistotalcost + thiswalkdist * (margprob - this.parameters.minimumMarginProbability);
            return thistotalcost;
        } else {
            thistotalcost = thistotalcost + thiswalkdist * margprob * sdprob;
        }
        lookedlist.add(sd);
        StationUtilityData closestneighbour = bestNeighbourReturn(sd.getStation(), newmargprob, lookedlist, allstats, destination);
        double newdist = sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition()) * this.parameters.bikefactor;
        if (start) {
            sd.closest = closestneighbour.getStation().getId();
            sd.closestbikedist = newdist;
            sd.closestprob = closestneighbour.getProbabilityReturn();
            sd.closestwalkdist = closestneighbour.getStation().getPosition().distanceTo(destination);
        }
        double margvalue = newmargprob * this.parameters.unsucesscostReturn + 
                calculateCostReturnHeuristic(closestneighbour, closestneighbour.getProbabilityReturn(), newmargprob, newdist, destination, lookedlist, allstats, false);
        return thistotalcost + this.parameters.penalisationfactorreturn * margvalue;
    }

    private StationUtilityData bestNeighbourRent(Station s, double newmargprob, List<StationUtilityData> lookedlist, List<StationUtilityData> allstats) {
        double newbestValueFound = (newmargprob - this.parameters.minimumMarginProbability) * this.parameters.MaxCostValue;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityTake() > this.parameters.minProbRecommendation) {
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
        double newbestValueFound = (newmargprob - this.parameters.minimumMarginProbability) * this.parameters.MaxCostValue;
        StationUtilityData bestneighbour = null;
        for (StationUtilityData nei : allstats) {
            if (!lookedlist.contains(nei) && nei.getProbabilityReturn() > this.parameters.minProbRecommendation) {
                double altthisbikedist = s.getPosition().distanceTo(nei.getStation().getPosition()) * this.parameters.bikefactor;
                double altthisbikecost = (newmargprob - this.parameters.minimumMarginProbability) * altthisbikedist;
                double altthiswalkdist = nei.getStation().getPosition().distanceTo(destination);
                double altnewmargprob = newmargprob * (1 - nei.getProbabilityReturn());
                double alttotalcost = altthisbikecost;
                if (altnewmargprob <= this.parameters.minimumMarginProbability) {
                    alttotalcost = alttotalcost + altthiswalkdist * (newmargprob - this.parameters.minimumMarginProbability);
                } else {
                    alttotalcost = alttotalcost + altthiswalkdist * newmargprob * nei.getProbabilityReturn();
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

 }
