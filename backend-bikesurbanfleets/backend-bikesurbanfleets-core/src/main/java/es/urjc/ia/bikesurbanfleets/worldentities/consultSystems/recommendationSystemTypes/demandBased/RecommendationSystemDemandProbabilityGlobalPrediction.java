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
@RecommendationSystemType("DEMAND_PROBABILITY_PREDICTION")
public class RecommendationSystemDemandProbabilityGlobalPrediction extends RecommendationSystem {

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
        private double requiredProbability=0.999;
        private double secondProbability=0.6;
        private double[][] probabilityorder = 
 /*           {{100,0.99}, {100,0.9}, {100,0.8}, {100,0.7}, 
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
*/          {{500,0.999}, {500,0.95},{500,0.9},{500,0.85},{500,0.8},{500,0.75},{500,0.7}, {500,0.65},{500,0.6},
             {10000,0.6},{10000,0.3}            
            };
        private double[] distancethreads = {100, 250, 500, 750, 1000};
        private double thirdProbability = 0.8;
        
        private double probabilityUsersObey = 1;
        private double factor=1D/(double)(maxDistanceRecommendation);
        private boolean takeintoaccountexpected=true;
        private boolean takeintoaccountcompromised=false;
    }

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
        //       List<Station> aux=validStationsToRentBike(infrastructureManager.consultStations());
        List<Station> aux = (infrastructureManager.consultStations());
        List<Station> stations = aux;//.stream()
        //         .filter(station -> station.getPosition().distanceTo(currentposition) <= parameters.maxDistanceRecommendation).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getStationUtilityRentBike(stations, currentposition);
            List<StationUtilityData> temp = su.stream().sorted(comp).collect(Collectors.toList());

            probst += temp.get(0).getProbability();
            callst++;
            //      if (temp.get(0).getProbability() < parameters.requiredProbability) {
            System.out.println();
            System.out.println("Time:" + SimulationDateTime.getCurrentSimulationDateTime());
            if (temp.get(0).getProbability() < parameters.requiredProbability) {
                System.out.format("LOW PROB %6f (take) %n",  temp.get(0).getProbability() );
                lowprobs++;
            }
            System.out.println("Expected successrate take:" + (probst / callst));
            temp.forEach(s -> 
{
  //              StationUtilityData s=temp.get(0);
                System.out.format("Station %3d (take) %2d %2d %10f %6f %n", + s.getStation().getId() ,
                         s.getStation().availableBikes() ,
                         s.getStation().getCapacity(),
                         s.getDistance() ,
                         s.getProbability());
            }
);
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
            List<StationUtilityData> su = getStationUtilityReturnBike(stations, destination, currentposition);
            List<StationUtilityData> temp = su.stream().sorted(comp).collect(Collectors.toList());
            probsr += temp.get(0).getProbability();
            callsr++;
            //     if (temp.get(0).getProbability() < parameters.requiredProbability) {
            System.out.println();
            System.out.println("Time:" + SimulationDateTime.getCurrentSimulationDateTime());
            if (temp.get(0).getProbability() < parameters.requiredProbability) {
                System.out.format("LOW PROB %6f (take) %n",  temp.get(0).getProbability() );
                lowprobs++;
            }
            lowprobs++;
            System.out.println("Expected successrate return:" + (probsr / callsr));
            temp.forEach(s -> 
            {

       //     StationUtilityData s=temp.get(0);
                System.out.format("Station %3d (return) %2d %2d %10f %6f %n", + s.getStation().getId() ,
                         s.getStation().availableBikes() ,
                         s.getStation().getCapacity(),
                         s.getDistance() ,
                         s.getProbability());
            }
            );
      //            }
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
        //}
    };

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
                return Double.compare(sq1.getDistance(), sq2.getDistance());
            }
            //if (sq1.getProbability()<p && sq2.getProbability()<p) {
         //   return Double.compare(sq2.getProbability(), sq1.getProbability());
            return Double.compare(sq1.getDistance(), sq2.getDistance());
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
        double v1=sq1.getProbability()-parameters.factor*sq1.getDistance();
        double v2=sq2.getProbability()-parameters.factor*sq2.getDistance();
        return Double.compare(v2,v1);
    };

    private int findindex(StationUtilityData sq1){
        double dist=sq1.getDistance();
        double util=sq1.getProbability();
        for (int i=0; i<parameters.probabilityorder.length; i++){
            if (dist<=parameters.probabilityorder[i][0] && util>=parameters.probabilityorder[i][1])
                return i;
        }
        return parameters.probabilityorder.length;
    }
    Comparator<StationUtilityData> special3 = (sq1, sq2) -> {
        int i1=findindex(sq1);
        int i2=findindex(sq2);
        if (i1<i2) return -1;
        if (i1>i2) return +1;
        return Double.compare(sq1.getDistance(),sq2.getDistance());
    };
    Comparator<StationUtilityData> comp=special0bis;

    public List<StationUtilityData> getStationUtilityRentBike(List<Station> stations, GeoPoint currentposition) {
        InfrastructureManager.UsageData ud = infrastructureManager.getCurrentUsagedata();
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);

            //get probability of renting or returning a bike sucessfully
            double dist = currentposition.distanceTo(s.getPosition());
            double off = dist / this.parameters.walkingVelocity;
            if (off<1) off=0;
            double prob = infrastructureManager.getAvailableBikeProbability(s, off,
                       parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            
            //now calculate the change in the global utility if the bike is taken/returned in this station
            // defined here like: prob(bikedemand, station)*change in probability of taking a bike +
            // prob(slotdemand, station)*change in probability of returning a bike 
            // the values are calculated at the time where the event would occure
            
            double posttakebikeprob = getPostAvailableBikeProbability(s, off,
                       parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            
            
            sd.setProbability(prob);
            sd.setDistance(dist);
            temp.add(sd);
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
            double off = dist / this.parameters.cyclingVelocity;
            if (off<1) off=0;
            prob = infrastructureManager.getAvailableSlotProbability(s, off,
                parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised);
            dist = s.getPosition().distanceTo(destination);
            
            //now calculate the change in the global utility if the bike is taken/returned in this station
            // defined here like: prob(bikedemand, station)*change in probability of taking a bike +
            // prob(slotdemand, station)*change in probability of returning a bike 
            // the values are calculated at the time where the event would occure
            
            
            sd.setProbability(prob);
            sd.setOptimalocupation(prob);
            sd.setDistance(dist);
            temp.add(sd);
        }
        return temp;
    }
    
    public double getPostAvailableBikeProbability(Station s, double timeoffset, boolean takeintoaccountexpected, boolean takeintoaccountcompromised) {
/*        int estimatedbikes = s.availableBikes()-1;
        if (takeintoaccountexpected) {
            infrastructureManager.getExpectedBikechanges(s.getId(), timeoffset); 
            estimatedbikes+= (int) Math.floor(changes* POBABILITY_USERSOBEY);
            if (takeintoaccountcompromised) {
    //            if ((estimatedbikes+minpostchanges)<=0){
                    estimatedbikes+= (int) Math.floor(minpostchanges* POBABILITY_USERSOBEY);
    //            }
            }
        }
        double takedemandattimeoffset = (infrastructureManager.getCurrentBikeDemand(s) * timeoffset) / 3600D;
        double retdemandatofsettime = (infrastructureManager.getCurrentSlotDemand(s) * timeoffset) / 3600D;
        //probability that a bike exists 
        int k = 1 - estimatedbikes;
        double probbike = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
        double probslot = SellamDistribution.calculateCDFSkellamProbability(retdemandatofsettime, takedemandattimeoffset, k);
*/
        return 1D;//prob;
    }

}
