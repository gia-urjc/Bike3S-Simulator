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
@RecommendationSystemType("DEMAND_PROBABILITY2")
public class RecommendationSystemDemandProbability2 extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        private double wheightDistanceStationUtility = 0.3;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12 / 1.25D;
        private double cyclingVelocity = 6.0 / 1.25D;
        private double requiredProbability = 0.999;
        private double secondProbability = 0.6;
        private double[][] probabilityorder
                = /*           {{100,0.99}, {100,0.9}, {100,0.8}, {100,0.7}, 
             {250,0.99}, {250,0.9}, {250,0.8}, {100,0.6}, {100,0.5}, {250,0.7}, {500,0.99}, {500,0.9}, 
             {500,0.8}, {250,0.6}, {250,0.5}, {500,0.7}, {500,0.6}, {750,0.99}, {750,0.9}, {500,0.5},
             {750,0.8}, {750,0.7}, {750,0.6}, {1000,0.99}, {750,0.5},
             {1000,0.9}, {1000,0.8}, {1000,0.7}, {1000,0.6}, {1000,0.5}            
            };
                 */ /*         {{100,0.9}, {100,0.7}, 
             {250,0.99}, {250,0.9}, {250,0.8}, {100,0.6}, {250,0.7}, {500,0.99}, {500,0.9}, 
             {500,0.8}, {250,0.6}, {500,0.7}, {500,0.6}, {750,0.99}, {750,0.9}, 
             {750,0.8}, {750,0.7}, {750,0.6}, {1000,0.99}, 
             {1000,0.9}, {1000,0.8}, {1000,0.7}, {1000,0.6}, {1000,0.5}, {10000,0.3}            
            };
                 */ /*  {{500,0.999}, {500,0.95},{500,0.9},{500,0.85},{500,0.8},{500,0.75},{500,0.7}, {500,0.65},{500,0.6},
             {10000,0.6},{10000,0.3}            
            };*/ {{600, 0.999},
                {600, 0.99}, {600, 0.98}, {600, 0.95}, {600, 0.92}, {600, 0.89}, {600, 0.86}, {600, 0.83}, {600, 0.8},
                {600, 0.77}, {600, 0.74}, {600, 0.71}, {600, 0.68}, {600, 0.65}, {600, 0.62},
                {600, 0.59},
                {800, 0.9}, {800, 0.75}, {800, 0.6},
                {10000, 0.6}
                };
        private double[] distancethreads = {100, 250, 500, 750, 1000};
        private double thirdProbability = 0.8;

        private double probabilityUsersObey = 1;
        private double factor = 1D / (double) (1000);
        private boolean takeintoaccountexpected = true;
        private boolean takeintoaccountcompromised = false;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbability2(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
            List<StationUtilityData> su = getStationUtility(stations, null, currentposition, true);
            printResults(su, true);
            result = su.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
            //add values to the expeted takes
            StationUtilityData first = su.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            this.infrastructureManager.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / this.parameters.walkingVelocity), true);
        } else {
            result = new ArrayList<>();
            System.out.println("no recommednation take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    private void printResults(List<StationUtilityData> su, boolean take) {
        int max = su.size();//Math.min(5, su.size());
        System.out.println();
        if (take) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime());
            probst += su.get(0).getProbability();
            callst++;
            System.out.println("Expected successrate take:" + (probst / callst));
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime());
            probsr += su.get(0).getProbability();
            callsr++;
            System.out.println("Expected successrate take:" + (probsr / callsr));
        }
        if (su.get(0).getProbability() < 0.6) {
            System.out.format("LOW PROB %6f %n", su.get(0).getProbability());
            lowprobs++;
        }
        for (int i = 0; i < max; i++) {
            StationUtilityData s = su.get(i);
            System.out.format("Station %3d %2d %2d %10.6f %6.10f %n", +s.getStation().getId(),
                    s.getStation().availableBikes(),
                    s.getStation().getCapacity(),
                    s.getDistance(),
                    s.getProbability());
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
            List<StationUtilityData> su = getStationUtility(stations, destination, currentposition, false);
            printResults(su, false);
            result = su.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
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

    Comparator<StationUtilityData> special0best = (sq1, sq2) -> {
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
                return Double.compare(sq1.getDistance(), sq2.getDistance());
            }
            //if (sq1.getProbability()<p && sq2.getProbability()<p) {
            return Double.compare(sq2.getProbability(), sq1.getProbability());
            //   return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getDistance() <= maxdist && sq1.getProbability() >= p2 && !(sq2.getProbability() >= p2 && sq2.getDistance() <= maxdist)) {
            return -1;
        }
        if (!(sq1.getDistance() <= maxdist && sq1.getProbability() >= p2) && (sq2.getProbability() >= p2 && sq2.getDistance() <= maxdist)) {
            return +1;
        }

        if (sq1.getProbability() >= p2 && sq2.getProbability() >= p2) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getProbability() >= p2 && sq2.getProbability() < p2) {
            return -1;
        }
        if (sq1.getProbability() < p2 && sq2.getProbability() >= p2) {
            return 1;
        }
        return Double.compare(sq1.getDistance(), sq2.getDistance());
    };

    Comparator<StationUtilityData> special = (sq1, sq2) -> {
        double p = this.parameters.requiredProbability;
        double p2 = this.parameters.secondProbability;
        double p3 = this.parameters.thirdProbability;
        double maxdist = this.parameters.maxDistanceRecommendation;
        //the best probs in the maximum distance are put first, ordered by distance
        if (sq1.getProbability() >= p && sq2.getProbability() >= p && sq1.getDistance() <= maxdist && sq2.getDistance() <= maxdist) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getProbability() >= p && sq1.getDistance() <= maxdist && !(sq2.getProbability() >= p && sq2.getDistance() <= maxdist)) {
            return -1;
        }
        if (!(sq1.getProbability() >= p && sq1.getDistance() <= maxdist) && sq2.getProbability() >= p && sq2.getDistance() <= maxdist) {
            return +1;
        }
        if (sq1.getProbability() == sq2.getProbability() && sq1.getDistance() == sq2.getDistance()) {
            return 0;
        }
        //second probability bound
        if (sq1.getProbability() >= p2 && sq2.getProbability() >= p2) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getProbability() >= p2 && !(sq2.getProbability() >= p2)) {
            return -1;
        }
        if (!(sq1.getProbability() >= p2) && sq2.getProbability() >= p2) {
            return +1;
        }
        //rest
        return Double.compare(sq1.getDistance(), sq2.getDistance());
    };

    Comparator<StationUtilityData> special2 = (sq1, sq2) -> {
        double v1 = sq1.getProbability() - parameters.factor * sq1.getDistance();
        double v2 = sq2.getProbability() - parameters.factor * sq2.getDistance();
        return Double.compare(v2, v1);
    };

    private int findindex(StationUtilityData sq1) {
        double dist = sq1.getDistance();
        double util = sq1.getProbability();
        for (int i = 0; i < parameters.probabilityorder.length; i++) {
            if (dist <= parameters.probabilityorder[i][0] && util >= parameters.probabilityorder[i][1]) {
                return i;
            }
        }
        return parameters.probabilityorder.length;
    }
    Comparator<StationUtilityData> special3 = (sq1, sq2) -> {
        int ret;
        int i1 = findindex(sq1);
        int i2 = findindex(sq2);
        if (i1 < i2) {
            ret = -1;
        } else if (i1 > i2) {
            ret = +1;
        } else {
            ret = Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        /*     int ret2=specialbesti.compare(sq1,sq2);
  /*      if (!((ret2>0 && ret>0) || (ret2==0 && ret==0) || (ret2<0 && ret<0)) ){
            System.out.println("different compare res3 resbis sq1 sq2:" + ret + " " + ret2
                    + " " + sq1.getDistance() + " " + sq1.getProbability()+ 
                    " " + sq2.getDistance() + " " + sq2.getProbability());   
        }
         */ return ret;
    };
    Comparator<StationUtilityData> comp = special0best;

    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint destination, GeoPoint currentposition, boolean rentbike) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        StationUtilityData best = null;
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);

            double prob = 0;
            double dist = 0;
            if (rentbike) {
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.walkingVelocity;
                if (off < 1) {
                    off = 0;
                }
                prob = infrastructureManager.getAvailableBikeProbability(s, off,
                        parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            } else {
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.cyclingVelocity;
                if (off < 1) {
                    off = 0;
                }
                prob = infrastructureManager.getAvailableSlotProbability(s, off,
                        parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
                dist = s.getPosition().distanceTo(destination);
            }
            sd.setProbability(prob);
            sd.setDistance(dist);
            if (best == null || betterOrSame(sd, best)) {
                best = sd;
            }
            add(sd, temp);
            //reduce computation time
            if (sd.getProbability()>this.parameters.requiredProbability) break;
        }
        return temp;
    }

    private boolean betterOrSame(StationUtilityData newSD,StationUtilityData oldSD){
        //case in the closer area
        //the best probs in the maximum distance are put first, ordered by distance
        if (newSD.getProbability() < oldSD.getProbability())  return false;
        if (newSD.getProbability() == oldSD.getProbability()){
            return false;
        }
        if (newSD.getProbability() > oldSD.getProbability()){
            if (oldSD.getProbability()<0.3) return true;
            double distdiff=(newSD.getDistance()-oldSD.getDistance())*this.parameters.factor;
            double probdiff=newSD.getProbability()-oldSD.getProbability();
            if (probdiff>distdiff) {
                return true;
            }
            return false;
        }
        return false;
    }
    private void add(StationUtilityData d, List<StationUtilityData> temp){
        int i=0;
        for (; i<temp.size(); i++){
            if (betterOrSame(d,temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }
    private static Comparator<Station> byDistance(GeoPoint point) {
        return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
    }

}
