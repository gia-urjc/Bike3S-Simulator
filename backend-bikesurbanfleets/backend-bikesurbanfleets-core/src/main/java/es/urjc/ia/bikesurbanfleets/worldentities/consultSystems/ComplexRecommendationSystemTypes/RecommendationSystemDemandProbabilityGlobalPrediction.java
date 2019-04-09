package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
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
@RecommendationSystemType("DEMAND_PROBABILITY_PREDICTION")
public class RecommendationSystemDemandProbabilityGlobalPrediction extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12 / 2D;
        private double cyclingVelocity = 6.0 / 2D;
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;

        private double probabilityUsersObey = 1;
        private double factorProb = 2000;
        private double factorImp = 500D;
        private boolean takeintoaccountexpected = true;
        private boolean takeintoaccountcompromised = true;
    }
    boolean printHints = true;

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
            List<StationUtilityData> su = getStationUtilityRentBike(stations, currentposition);
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
                    (int) (dist / this.parameters.walkingVelocity), true);
        } else {
            result = new ArrayList<>();
            System.out.println("no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());

        }
        return result;
    }

    private int lowprobs = 0;
    private double probsr = 0D;
    private int callsr = 0;
    private double probst = 0D;
    private int callst = 0;

    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
//       List<Station> aux=validStationsToReturnBike(infrastructureManager.consultStations());
        List<Station> stations = infrastructureManager.consultStations().stream().
                sorted(byDistance(destination)).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtilityReturnBike(stations, destination, currentposition);
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
                    (int) (dist / this.parameters.cyclingVelocity), false);
        } else {
            System.out.println("no recommednation return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());

        }
        return result;
    }

    private void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
            int max = Math.min(5, su.size());
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
            for (int i = 0; i < max; i++) {
                StationUtilityData s = su.get(i);
                System.out.format("Station %3d %2d %2d %10.2f %9.8f %9.8f %n", +s.getStation().getId(),
                        s.getStation().availableBikes(),
                        s.getStation().getCapacity(),
                        s.getWalkdist(),
                        s.getUtility(),
                        s.getProbability());
            }
        }
    }

    /*
    Comparator<StationUtilityData> special0bis = (sq1, sq2) -> {
        double p = this.parameters.requiredProbability;
        double p2 = this.parameters.secondProbability;
        double p3 = this.parameters.thirdProbability;
        double maxdist = this.parameters.maxDistanceRecommendation;
        //case in the closer area
        //the best probs in the maximum distance are put first, ordered by distance
        if (sq1.getDistance() <= maxdist && sq2.getDistance() <= maxdist && sq1.getProbability() >= p2 && sq2.getProbability() >= p2) {
            if (sq1.getProbability() >= p && sq2.getProbability() < p) {
                return -1;
            }
            if (sq1.getProbability() < p && sq2.getProbability() >= p) {
                return +1;
            }
            if (sq1.getProbability() >= p && sq2.getProbability() >= p) {
                return Double.compare(sq2.getUtility(), sq1.getUtility());
            }
            //if (sq1.getProbability()<p && sq2.getProbability()<p) {
         //   return Double.compare(sq2.getProbability(), sq1.getProbability());
            return Double.compare(sq2.getUtility(), sq1.getUtility());
        }
        if (sq1.getDistance() <= maxdist && sq1.getProbability() >= p2 && !(sq2.getProbability() >= p2 && sq2.getDistance() <= maxdist)) {
            return -1;
        }
        if (!(sq1.getDistance() <= maxdist && sq1.getProbability() >= p2) && (sq2.getProbability() >= p2 && sq2.getDistance() <= maxdist)) {
            return +1;
        }

        if (sq1.getProbability() >= p2 && sq2.getProbability() >= p2) {
            return Double.compare(sq2.getUtility(), sq1.getUtility());
        }
        if (sq1.getProbability() >= p2 && sq2.getProbability() < p2) {
            return -1;
        }
        if (sq1.getProbability() < p2 && sq2.getProbability() >= p2) {
            return 1;
        }
        return Double.compare(sq2.getUtility(), sq1.getUtility());
    };

     Comparator<StationUtilityData> c1 = (sq1, sq2) -> {
        double p = this.parameters.requiredProbability;
        double p2 = this.parameters.secondProbability;
        double p3 = this.parameters.thirdProbability;
        double maxdist = this.parameters.maxDistanceRecommendation;
        //case in the closer area
        //the best probs in the maximum distance are put first, ordered by distance
        if (sq1.getDistance() <= maxdist && sq2.getDistance() <= maxdist) {
                return Double.compare(sq2.getUtility(), sq1.getUtility());
        }
        if (sq1.getDistance() <= maxdist) {
            return -1;
        }
        if (sq2.getDistance() <= maxdist) {
            return +1;
        }
            return Double.compare(sq2.getUtility(), sq1.getUtility());
    };
      
    Comparator<StationUtilityData> comp=c1;
     */
    public List<StationUtilityData> getStationUtilityRentBike(List<Station> stations, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);

            //get probability of renting or returning a bike sucessfully
            double dist = currentposition.distanceTo(s.getPosition());
            int offtime = (int) (dist / this.parameters.walkingVelocity);
            double prob = infrastructureManager.getAvailableBikeProbability(s, offtime,
                    parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);

            //now calculate the change in the global utility if the bike is taken/returned in this station
            // defined here like: prob(bikedemand, station)*change in probability of taking a bike +
            // prob(slotdemand, station)*change in probability of returning a bike 
            // the values are calculated at the time where the event would occure
            sd.setUtility(getGlobalProbabilityImprovementIfTake(
                    s, offtime, parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised));

            sd.setProbability(prob).setWalkdist(dist);
            addrent(sd, temp);
        }
        return temp;
    }

    public List<StationUtilityData> getStationUtilityReturnBike(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);

            //get probability of renting or returning a bike sucessfully
            double prob = 0;
            double dist = currentposition.distanceTo(s.getPosition());
            int offtime = (int) (dist / this.parameters.cyclingVelocity);
            prob = infrastructureManager.getAvailableSlotProbability(s, offtime,
                    parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            dist = s.getPosition().distanceTo(destination);

            //now calculate the change in the global utility if the bike is taken/returned in this station
            // defined here like: prob(bikedemand, station)*change in probability of taking a bike +
            // prob(slotdemand, station)*change in probability of returning a bike 
            // the values are calculated at the time where the event would occure
            sd.setUtility(getGlobalProbabilityImprovementIfReturn(
                    s, offtime, parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised));

            sd.setProbability(prob).setWalkdist(dist);
            addreturn(sd, temp);
        }
        return temp;
    }

    public double getGlobalProbabilityImprovementIfTake(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {

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

    public double getGlobalProbabilityImprovementIfReturn(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {

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
        double probbikediff = SellamDistribution.calculateSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        k = 1 - estimatedslots;
        double probslotdiff = -SellamDistribution.calculateSkellamProbability(takedemandattimeoffset, retdemandatofsettime, k);

        //now calculate the demands at the future point relative to the global demand
        double futtakedemand = infrastructureManager.getFutureBikeDemand(s, (int) timeoffset);
        double futreturndemand = infrastructureManager.getFutureSlotDemand(s, (int) timeoffset);
        double futglobaltakedem = infrastructureManager.getFutureGlobalBikeDemand((int) timeoffset);
        double futglobalretdem = infrastructureManager.getFutureGlobalSlotDemand((int) timeoffset);

        double relativeimprovemente = (futtakedemand / futglobaltakedem) * probbikediff
                + (futreturndemand / futglobalretdem) * probslotdiff;
        return relativeimprovemente;
    }
    private boolean decideByGlobalUtility(StationUtilityData newSD, StationUtilityData oldSD) {
            double timediff = (newSD.getWalkdist()- oldSD.getWalkdist());
            double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
            double probdiff = (newSD.getProbability() - oldSD.getProbability())* this.parameters.factorProb;
            if ((utildiff+probdiff) > (timediff)) {
                    return true;
                }
                return false;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        // if here newSD.getProbability() > oldSD.getProbability()
        if (newSD.getWalkdist()<= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbability() > this.parameters.upperProbabilityBound && newSD.getProbability() > this.parameters.upperProbabilityBound) {
                return decideByGlobalUtility(newSD, oldSD);
            }
            if (oldSD.getProbability() > this.parameters.upperProbabilityBound ) return false;
            if (newSD.getProbability() > this.parameters.upperProbabilityBound ) return true;
            
            if (oldSD.getProbability() > this.parameters.desireableProbability && newSD.getProbability() > this.parameters.desireableProbability) {
                return decideByGlobalUtility(newSD, oldSD);
            }
            if (oldSD.getProbability() > this.parameters.desireableProbability ) return false;
            if (newSD.getProbability() > this.parameters.desireableProbability ) return true;
            if (newSD.getProbability() > oldSD.getProbability() ) return true;

            return false;
        }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
                return decideByGlobalUtility(newSD, oldSD);
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
                return decideByGlobalUtility(newSD, oldSD);
     }

    private void addrent(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameRent(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }

    private void addreturn(StationUtilityData d, List<StationUtilityData> temp) {
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
