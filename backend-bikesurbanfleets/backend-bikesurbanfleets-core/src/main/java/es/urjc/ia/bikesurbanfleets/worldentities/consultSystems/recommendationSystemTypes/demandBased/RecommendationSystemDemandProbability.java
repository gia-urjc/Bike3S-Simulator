package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.demandBased;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
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
@RecommendationSystemType("DEMAND_PROBABILITY")
public class RecommendationSystemDemandProbability extends RecommendationSystem {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        private double wheightDistanceStationUtility = 0.3;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double walkingVelocity = 1.12/1.25D;
        private double cyclingVelocity = 6.0/1.25D;
        private double requiredProbability = 0.999;
        private double secondProbability = 0.6;
        private double thirdProbability = 0.3;
        private double probabilityUsersObey = 1;
        private double factor=1D/(double)(maxDistanceRecommendation);
        private boolean takeintoaccountexpected=true;
        private boolean takeintoaccountcompromised=true;
    }

    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbability(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        //       List<Station> aux=validStationsToRentBike(infrastructureManager.consultStations());
        List<Station> aux = (infrastructureManager.consultStations());
        List<Station> stations = aux;//.stream()
        //         .filter(station -> station.getPosition().distanceTo(currentposition) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, null, currentposition, true);
            List<StationUtilityData> temp = su.stream().sorted(special0bis).collect(Collectors.toList());

            probst += temp.get(0).getUtility();
            callst++;
            //      if (temp.get(0).getUtility() < parameters.requiredProbability) {
            System.out.println();
            System.out.println("Time:" + SimulationDateTime.getCurrentSimulationDateTime());
            if (temp.get(0).getUtility() < parameters.requiredProbability) {
                System.out.println("LOW PROB take " + temp.get(0).getUtility() + " " + lowprobs);
                lowprobs++;
            }
            System.out.println("Expected successrate take:" + (probst / callst));
//            temp.forEach(s -> 
{
                StationUtilityData s=temp.get(0);
                System.out.println("Station " + s.getStation().getId() + " (take): "
                        + s.getStation().availableBikes() + " "
                        + s.getStation().getCapacity() + " "
                        + s.getOptimalocupation() + " "
                        + s.getDistance() + " "
                        + s.getUtility());

            }
//);
            //}
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());
            //add values to the expeted takes
            StationUtilityData first = temp.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            this.infrastructureManager.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / this.parameters.walkingVelocity), true);
        } else {
            result = new ArrayList<>();
            System.out.println("no recommednation take at Time:" + SimulationDateTime.getCurrentSimulationDateTime());

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
        List<Station> aux = (infrastructureManager.consultStations());
        List<Station> stations = aux;//.stream().
        //            filter(station -> station.getPosition().distanceTo(destination) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtility(stations, destination, currentposition, false);
            List<StationUtilityData> temp = su.stream().sorted(special0bis).collect(Collectors.toList());
            probsr += temp.get(0).getUtility();
            callsr++;
            //     if (temp.get(0).getUtility() < parameters.requiredProbability) {
            System.out.println();
            System.out.println("Time:" + SimulationDateTime.getCurrentSimulationDateTime());
            if (temp.get(0).getUtility() < parameters.requiredProbability) {
                System.out.println("LOW PROB return " + temp.get(0).getUtility() + " " + lowprobs);
                lowprobs++;
            }
            lowprobs++;
            System.out.println("Expected successrate return:" + (probsr / callsr));
        //    temp.forEach(s -> 
            {

            StationUtilityData s=temp.get(0);
                    System.out.println("Station " + s.getStation().getId() + " (return): "
                        + s.getStation().availableBikes() + " "
                        + s.getStation().getCapacity() + " "
                        + s.getOptimalocupation() + " "
                        + s.getDistance() + " "
                        + s.getUtility());

            }
            //);
            //      }
            result = temp.stream().map(sq -> new Recommendation(sq.getStation(), null)).collect(Collectors.toList());

            //add values to the expeted returns
            StationUtilityData first = temp.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            this.infrastructureManager.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / this.parameters.cyclingVelocity), false);
        } else {
            System.out.println("no recommednation return at Time:" + SimulationDateTime.getCurrentSimulationDateTime());

        }
        return result;
    }
    //original comp
    Comparator<StationUtilityData> special0 = (sq1, sq2) -> {
        double p = this.parameters.requiredProbability;
        if (sq1.getUtility() >= p && sq2.getUtility() < p) {
            return -1;
        }
        if (sq1.getUtility() < p && sq2.getUtility() >= p) {
            return +1;
        }
        if (sq1.getUtility() >= p && sq2.getUtility() >= p) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        //if (sq1.getUtility()<p && sq2.getUtility()<p) {
        return Double.compare(sq2.getUtility(), sq1.getUtility());
        //}
    };

    Comparator<StationUtilityData> special0bis = (sq1, sq2) -> {
        double p = this.parameters.requiredProbability;
        double p2 = this.parameters.secondProbability;
        double p3 = this.parameters.thirdProbability;
        double maxdist = this.parameters.maxDistanceRecommendation;
        //case in the closer area
        //the best probs in the maximum distance are put first, ordered by distance
        if (sq1.getDistance() <= maxdist && sq2.getDistance() <= maxdist && sq1.getUtility() >= p2 && sq2.getUtility() >= p2) {
            if (sq1.getUtility() >= p && sq2.getUtility() < p) {
                return -1;
            }
            if (sq1.getUtility() < p && sq2.getUtility() >= p) {
                return +1;
            }
            if (sq1.getUtility() >= p && sq2.getUtility() >= p) {
                return Double.compare(sq1.getDistance(), sq2.getDistance());
            }
            //if (sq1.getUtility()<p && sq2.getUtility()<p) {
            return Double.compare(sq2.getUtility(), sq1.getUtility());
        //    return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getDistance() <= maxdist && sq1.getUtility() >= p2 && !(sq2.getUtility() >= p2 && sq2.getDistance() <= maxdist)) {
            return -1;
        }
        if (!(sq1.getDistance() <= maxdist && sq1.getUtility() >= p2) && (sq2.getUtility() >= p2 && sq2.getDistance() <= maxdist)) {
            return +1;
        }

        if (sq1.getUtility() >= p2 && sq2.getUtility() >= p2) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getUtility() >= p2 && sq2.getUtility() < p2) {
            return -1;
        }
        if (sq1.getUtility() < p2 && sq2.getUtility() >= p2) {
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
        if (sq1.getUtility() >= p && sq2.getUtility() >= p && sq1.getDistance() <= maxdist && sq2.getDistance() <= maxdist) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getUtility() >= p && sq1.getDistance() <= maxdist && !(sq2.getUtility() >= p && sq2.getDistance() <= maxdist)) {
            return -1;
        }
        if (!(sq1.getUtility() >= p && sq1.getDistance() <= maxdist) && sq2.getUtility() >= p && sq2.getDistance() <= maxdist) {
            return +1;
        }
        if (sq1.getUtility() == sq2.getUtility() && sq1.getDistance() == sq2.getDistance()) {
            return 0;
        }
        //second probability bound
        if (sq1.getUtility() >= p2 && sq2.getUtility() >= p2) {
            return Double.compare(sq1.getDistance(), sq2.getDistance());
        }
        if (sq1.getUtility() >= p2 && !(sq2.getUtility() >= p2)) {
            return -1;
        }
        if (!(sq1.getUtility() >= p2) && sq2.getUtility() >= p2) {
            return +1;
        }
        //rest
        return Double.compare(sq1.getDistance(), sq2.getDistance());
    };
            
    
    Comparator<StationUtilityData> special2 = (sq1, sq2) -> {
        double v1=sq1.getUtility()-parameters.factor*sq1.getDistance();
        double v2=sq2.getUtility()-parameters.factor*sq2.getDistance();
        return Double.compare(v2,v1);
    };

    public List<StationUtilityData> getStationUtility(List<Station> stations, GeoPoint destination, GeoPoint currentposition, boolean rentbike) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {

            StationUtilityData sd = new StationUtilityData(s);

            double prob = 0;
            double dist = 0;
            if (rentbike) {
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.walkingVelocity;
                if (off<1) off=0;
                prob = infrastructureManager.getAvailableBikeProbability(s, off,
                       parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            } else {
                dist = currentposition.distanceTo(s.getPosition());
                double off = dist / this.parameters.cyclingVelocity;
                if (off<1) off=0;
                prob = infrastructureManager.getAvailableSlotProbability(s, off,
                parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
                dist = s.getPosition().distanceTo(destination);
            }

            //           double norm_distance = 1 - normatizeTo01(dist, 0, parameters.maxDistanceRecommendation);
            //           double globalutility = parameters.wheightDistanceStationUtility * norm_distance
            //                   + (1 - parameters.wheightDistanceStationUtility) * normedUtilityDiff;
            //           double globalutility =  norm_distance * prob;

            /*       double mincap=(double)infraestructureManager.getMinStationCapacity();
            double maxinc=(4D*(mincap-1))/Math.pow(mincap,2);
            double auxnormutil=((newutility-utility+maxinc)/(2*maxinc));
            double globalutility= dist/auxnormutil; 
             */
            sd.setUtility(prob);
            sd.setOptimalocupation(prob);
            sd.setDistance(dist);
            temp.add(sd);
        }
        return temp;
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
}
