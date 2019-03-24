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
@RecommendationSystemType("DEMAND_cost_1")
public class RecommendationSystemDemandProbabilityCost_1 extends RecommendationSystem {

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
        private double desireableProbability = 0.6;

        private double probabilityUsersObey = 1;
        private double factor = 1D / (double) (1000);
    }

    boolean takeintoaccountexpected = true;
    boolean takeintoaccountcompromised = true;

    boolean printHints = true;
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityCost_1(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
            List<StationUtilityData> su = getStationUtilityRent(stations, currentposition);
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
                System.out.format("Station %3d %2d %2d %10.2f %10.2f %9.8f %9.8f %n", +s.getStation().getId(),
                        s.getStation().availableBikes(),
                        s.getStation().getCapacity(),
                        s.getDistance(),
                        s.getTime(),
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

    public List<StationUtilityData> getStationUtilityRent(List<Station> stations, GeoPoint currentposition) {
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
            temp.add(sd);
        }
        //now calculate the costs
        for (StationUtilityData sd : temp) {
            List<StationUtilityData> lookedlist = new ArrayList<>();
            double cost = calculateCostRent(sd, temp);
            sd.setUtility(cost);
            addRent(sd, res);
        }
        return res;
    }

    public List<StationUtilityData> getStationUtilityReturn(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        List<StationUtilityData> res = new ArrayList<>();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double dist=currentposition.distanceTo(s.getPosition());
            int offtime = (int) (dist / this.parameters.cyclingVelocity);
            double prob = infrastructureManager.getAvailableSlotProbability(s, offtime,
                    takeintoaccountexpected, takeintoaccountcompromised);
            dist=(s.getPosition().distanceTo(destination));
            int time = (int) ((currentposition.distanceTo(s.getPosition()) / this.parameters.cyclingVelocity)
                    + (s.getPosition().distanceTo(destination) / this.parameters.walkingVelocity));
            sd.setProbability(prob).setTime(time).setDistance(dist);
            temp.add(sd);
        }
        //now calculate the costs
        for (StationUtilityData sd : temp) {
            double cost = calculateCostReturn(sd, temp, destination,currentposition);
            sd.setUtility(cost);
            addReturn(sd, res);
        }
        return res;
    }

    private double calculateCostRent(StationUtilityData sd, List<StationUtilityData> stations) {
        double p=sd.getProbability();
        double d=sd.getDistance();
        StationUtilityData closestneighbour = getClosestNeighbour(sd, stations);
        double d2 = closestneighbour.getStation().getPosition().distanceTo(sd.getStation().getPosition());
        double p2=closestneighbour.getProbability();
        return p*d+(1-p)*2*(
                    p2*(d+d2)+ (1-p2)*2*
                            (d+d2+1000)
                );
    }
    private double calculateCostReturn(StationUtilityData sd, List<StationUtilityData> stations, GeoPoint destination, GeoPoint current) {

 /*       StationUtilityData closestneighbour = getClosestNeighbour(sd, stations);
        double firstbiketime=sd.getDistance()/this.parameters.cyclingVelocity;
        double firstwalktime=sd.getStation().getPosition().distanceTo(destination)/this.parameters.walkingVelocity;
        double secondbiketime=sd.getStation().getPosition().distanceTo(closestneighbour.getStation().getPosition())/this.parameters.cyclingVelocity;
        double secondwalktime=closestneighbour.getStation().getPosition().distanceTo(destination)/this.parameters.walkingVelocity;
        return firstbiketime+
                sd.getProbability()* firstwalktime+
                (1 - sd.getProbability())*20000;//(secondbiketime+secondwalktime);
*/
        StationUtilityData closestneighbour = getClosestNeighbour(sd, stations);
        double p=sd.getProbability();
        double db=current.distanceTo(sd.getStation().getPosition())/10;
        double dw=sd.getDistance();
        double p2=closestneighbour.getProbability();
        double db2 = closestneighbour.getStation().getPosition().distanceTo(sd.getStation().getPosition())/10;
        double dw2 = closestneighbour.getStation().getPosition().distanceTo(destination);
        return p*(db+dw)+(1-p)*2*(
                p2*(db+db2+dw2)+(1-p2)*2*(db+db2+2000)
                );
    }

    private StationUtilityData getClosestNeighbour(StationUtilityData sd, List<StationUtilityData> allstats) {
        StationUtilityData closest = null;
        double clostesdist = Double.MAX_VALUE;
        for (StationUtilityData nei : allstats) {
            if (nei.equals(sd)) {
                continue;
            }
            double dist = nei.getStation().getPosition().distanceTo(sd.getStation().getPosition());
            if (dist < clostesdist) {
                clostesdist = dist;
                closest = nei;
            }
        }

        return closest;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            if (newSD.getUtility() < oldSD.getUtility()) {
                return true;
            } else {
                return false;
            }
        }
        if (oldSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getUtility() < oldSD.getUtility()) {
            return true;
        } else {
            return false;
        }
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getUtility() < oldSD.getUtility()) {
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
