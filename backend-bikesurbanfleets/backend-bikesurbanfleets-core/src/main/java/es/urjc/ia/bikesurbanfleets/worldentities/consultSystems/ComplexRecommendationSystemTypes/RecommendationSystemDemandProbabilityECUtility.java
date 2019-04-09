package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.simpleRecommendationSystemTypes.StationComparator;
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
@RecommendationSystemType("DEMAND_PROBABILITY_expected_compromised_UTILITY")
public class RecommendationSystemDemandProbabilityECUtility extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12 / 2D;//2.25D; //with 3 the time is quite worse
        private double cyclingVelocity = 6.0 / 2D;//2.25D; //reduciendo este factor mejora el tiempo, pero empeora los indicadores 
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;

        private double probabilityUsersObey = 1;
        private double factorProb = 2000D;
        private double factorImp = 1000D;
    }

    boolean takeintoaccountexpected = true;
    boolean takeintoaccountcompromised = true;

    boolean printHints = true;
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityECUtility(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
            List<StationUtilityData> su = getStationRecomendationRent(stations, currentposition);
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
                System.out.format("Station %3d %2d %2d %10.2f %9.8f %16.14f %n", +s.getStation().getId(),
                        s.getStation().availableBikes(),
                        s.getStation().getCapacity(),
                        s.getWalkdist(),
                        s.getProbability(),
                        s.getUtility());
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
            List<StationUtilityData> su = getStationRecomendationReturn(stations, destination, currentposition);
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
            System.out.println("no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());
        }
        return result;
    }

    public List<StationUtilityData> getStationRecomendationRent(List<Station> stations, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
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
            sd.setWalkdist(dist);
            double util = calculateStationUtility(s, true);
            sd.setUtility(util);
            addrent(sd, temp);
            //reduce computation time
            //        if (sd.getProbability() > 0.999 && sd.getDistance() < 2000) {
            //            break;
            //        }
        }
        return temp;
    }

    public List<StationUtilityData> getStationRecomendationReturn(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        StationUtilityData best = null;

        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double dist = currentposition.distanceTo(s.getPosition());
            double off = dist / this.parameters.cyclingVelocity;
            if (off < 1) {
                off = 0;
            }
            double prob = infrastructureManager.getAvailableSlotProbability(s, off,
                    takeintoaccountexpected, takeintoaccountcompromised);
            dist = s.getPosition().distanceTo(destination);
            sd.setProbability(prob).setWalkdist(dist);
            double util = calculateStationUtility(s, false);
            sd.setUtility(util);
            addreturn(sd, temp);


            //reduce computation time
            /*         if (best == null || betterOrSameRent(sd, best)) {
                best = sd;
            }
   /*         if (sd.getProbability() > 0.999 && sd.getDistance() < 2000) {
                break;
            }
             */        }
        return temp;
    }

    public double calculateStationUtility(Station s, boolean rentbike) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();

        double idealbikes = infrastructureManager.getCurrentBikeDemand(s);
        double maxidealbikes = s.getCapacity() - infrastructureManager.getCurrentSlotDemand(s);
        double currentutility = getUtility(s, 0, idealbikes, maxidealbikes);
        double newutility;
        if (rentbike) {
            newutility = getUtility(s, -1, idealbikes, maxidealbikes);
        } else {//return bike 
            newutility = getUtility(s, +1, idealbikes, maxidealbikes);
        }
        double normedUtilityDiff = (newutility - currentutility)
                * (idealbikes / ud.currentGlobalBikeDemand) * ud.numberStations;

        return normedUtilityDiff;
    }

    private double getUtility(Station s, int bikeincrement, double idealbikes, double maxidealbikes) {
        double cap = s.getCapacity();
        double ocupation = s.availableBikes() + bikeincrement;
        if (idealbikes <= maxidealbikes) {
            if (ocupation <= idealbikes) {
                return 1 - Math.pow(((ocupation - idealbikes) / idealbikes), 2);
            } else if (ocupation >= maxidealbikes) {
                return 1 - Math.pow(((ocupation - maxidealbikes) / (cap - maxidealbikes)), 2);
            } else {//if ocupation is just between max and min
                return 1;
            }
        } else { //idealbikes > max idealbikes
            double bestocupation = (idealbikes + maxidealbikes) / 2D;
            //          double bestocupation = (idealbikes * cap)/(cap - maxidealbikes  ) ;
            if (ocupation <= bestocupation) {
                return 1 - Math.pow(((ocupation - bestocupation) / bestocupation), 2);
            } else {
                double aux = cap - bestocupation;
                return 1 - Math.pow(((ocupation - bestocupation) / aux), 2);
            }

        }
    }


    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (oldSD.getWalkdist()<= this.parameters.maxDistanceRecommendation) {
            // if here newSD.getProbability() > oldSD.getProbability()
            if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
                double distdiff = (newSD.getWalkdist() - oldSD.getWalkdist());
                double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
                double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
                if ((probdiff + utildiff) > (distdiff)) {
                    return true;
                }
                return false;
            }
            return false;
        }
        double distdiff = (newSD.getWalkdist() - oldSD.getWalkdist());
        double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
        double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
        if ((probdiff + utildiff) > (distdiff)) {
            return true;
        }
        return false;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        double distdiff = (newSD.getWalkdist() - oldSD.getWalkdist());
        double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
        double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
        if ((probdiff + utildiff) > (distdiff)) {
            return true;
        }
        return false;
    }
/*
    //for comparison and generating the outbut
    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbability() < this.parameters.desireableProbability) {
                //serach for the highest probability
                if (newSD.getProbability() > oldSD.getProbability()) {
                    return true;
                } else {
                    return false;
                }
            }
            if (oldSD.getProbability() >= this.parameters.upperProbabilityBound) {
                if (newSD.getProbability() < this.parameters.upperProbabilityBound) {
                    return false;
                } else {
                    //find a tradeoff    
                    double distdiff = (newSD.getDistance() - oldSD.getDistance());
                    double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
                    if ((utildiff) > (distdiff)) {
                        return true;
                    }
                    return false;
                }
            }
            if (oldSD.getProbability() >= this.parameters.desireableProbability) {
                if (newSD.getProbability() < this.parameters.desireableProbability) {
                    return false;
                } else {
                    //find a tradeoff    
                    double distdiff = (newSD.getDistance() - oldSD.getDistance());
                    double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
                    double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
                    if ((probdiff + utildiff) > (2 * distdiff)) {
                        return true;
                    }
                    return false;
                }
            }
        }
        if (oldSD.getDistance() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        double distdiff = (newSD.getDistance() - oldSD.getDistance());
        double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
        double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
        if ((probdiff + utildiff) > (2 * distdiff)) {
            return true;
        }
        return false;
    }

    //take into account that distance newSD >= distance oldSD
    private boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        if (oldSD.getProbability() < this.parameters.desireableProbability) {
            //serach for the highest probability
            if (newSD.getProbability() > oldSD.getProbability()) {
                return true;
            } else {
                return false;
            }
        }
        if (oldSD.getProbability() >= this.parameters.upperProbabilityBound) {
            if (newSD.getProbability() < this.parameters.upperProbabilityBound) {
                return false;
            } else {
                //find a tradeoff    
                double distdiff = (newSD.getDistance() - oldSD.getDistance());
                double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
                if ((utildiff) > (distdiff)) {
                    return true;
                }
                return false;
            }
        }
        if (oldSD.getProbability() >= this.parameters.desireableProbability) {
            if (newSD.getProbability() < this.parameters.desireableProbability) {
                return false;
            } else {
                //find a tradeoff    
                double distdiff = (newSD.getDistance() - oldSD.getDistance());
                double probdiff = (newSD.getProbability() - oldSD.getProbability()) * this.parameters.factorProb;
                double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
                if ((probdiff + utildiff) > (2 * distdiff)) {
                    return true;
                }
                return false;
            }
        } 
        return false;
    }
     */
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
