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
@RecommendationSystemType("DEMAND_PROBABILITY_expected_compromised")
public class RecommendationSystemDemandProbabilityEC extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 1000;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12 / 2D;//2.25D; //with 3 the time is quite worse
        private double cyclingVelocity = 6.0 / 2D;//2.25D; //reduciendo este factor mejora el tiempo, pero empeora los indicadores 
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6; 

        private double probabilityUsersObey = 1;
        private double factor = 1D / (double) (2000);
    }
    
    boolean takeintoaccountexpected = true;
    boolean takeintoaccountcompromised = true;

    boolean printHints=false;
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityEC(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
            List<StationUtilityData> su = getStationUtilityRent(stations, null, currentposition);
            if (printHints) printRecomendations(su, true);
            result = su.stream().map(sq -> {
                Recommendation r=new Recommendation(sq.getStation(), null);
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

    private void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
        int max = su.size();//Math.min(5, su.size());
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
                    s.getDistance(),
                    s.getUtility(),
                    s.getProbability());
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
            List<StationUtilityData> su = getStationUtilityReturn(stations, destination, currentposition);
            if (printHints) printRecomendations(su, false);
            result = su.stream().map(sq -> {
                Recommendation r=new Recommendation(sq.getStation(), null);
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
            System.out.println("no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<StationUtilityData> getStationUtilityRent(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        StationUtilityData best = null;
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);

            double prob = 0;
            double dist = 0;
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.walkingVelocity;
                if (off < 1) {
                    off = 0;
                }
                prob = infrastructureManager.getAvailableBikeProbability(s, off,
                        takeintoaccountexpected, takeintoaccountcompromised);
            sd.setProbability(prob);
            sd.setDistance(dist);
            if (best == null || betterOrSameRent(sd, best)) {
                best = sd;
            }
            addrent(sd, temp);
            //reduce computation time
            if (sd.getProbability()>0.999 && sd.getDistance()<2000) break;
        }
        return temp;
    }
    public List<StationUtilityData> getStationUtilityReturn(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        StationUtilityData best = null;
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);

            double prob = 0;
            double dist = 0;
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.cyclingVelocity;
                if (off < 1) {
                    off = 0;
                }
                prob = infrastructureManager.getAvailableSlotProbability(s, off,
                        takeintoaccountexpected, takeintoaccountcompromised);
                dist = s.getPosition().distanceTo(destination);
            sd.setProbability(prob);
            sd.setDistance(dist);
            if (best == null || betterOrSameReturn(sd, best)) {
                best = sd;
            }
            addreturn(sd, temp);
            //reduce computation time
            if (sd.getProbability()>0.999 && sd.getDistance()<2000) break;
        }
        return temp;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD,StationUtilityData oldSD){
        if (oldSD.getProbability()>this.parameters.upperProbabilityBound) return false;
        if (newSD.getProbability() <= oldSD.getProbability())  return false;
        // if here newSD.getProbability() > oldSD.getProbability()
        if (newSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbability()>this.parameters.desireableProbability ) {
                double distdiff=(newSD.getDistance()-oldSD.getDistance())*this.parameters.factor;
                double probdiff=newSD.getProbability()-oldSD.getProbability();
                if (probdiff>distdiff) return true;
                return false;
            }
            return true;
        }
        if (oldSD.getDistance() <= this.parameters.maxDistanceRecommendation ) return false;
        double distdiff=(newSD.getDistance()-oldSD.getDistance())*this.parameters.factor;
        double probdiff=newSD.getProbability()-oldSD.getProbability();
        if (probdiff>distdiff) return true;
        return false;
    }
 
    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD,StationUtilityData oldSD){
        if (oldSD.getProbability()>this.parameters.upperProbabilityBound) return false;
        if (newSD.getProbability() <= oldSD.getProbability())  return false;
        // if here  newSD.getProbability() > oldSD.getProbability()
        if (oldSD.getProbability() > this.parameters.desireableProbability) {
            double distdiff=(newSD.getDistance()-oldSD.getDistance())*this.parameters.factor;
            double probdiff=newSD.getProbability()-oldSD.getProbability();
            if (probdiff>distdiff) return true;
            return false;
        } 
        if (newSD.getProbability() >=this.parameters.desireableProbability) return true;  
        double distdiff=(newSD.getDistance()-oldSD.getDistance())*this.parameters.factor;
        double probdiff=newSD.getProbability()-oldSD.getProbability();
        if (probdiff>distdiff) return true;
        return false;
    }

    private void addrent(StationUtilityData d, List<StationUtilityData> temp){
        int i=0;
        for (; i<temp.size(); i++){
            if (betterOrSameRent(d,temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }
    private void addreturn(StationUtilityData d, List<StationUtilityData> temp){
        int i=0;
        for (; i<temp.size(); i++){
            if (betterOrSameReturn(d,temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }
    private static Comparator<Station> byDistance(GeoPoint point) {
        return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
    }

}
