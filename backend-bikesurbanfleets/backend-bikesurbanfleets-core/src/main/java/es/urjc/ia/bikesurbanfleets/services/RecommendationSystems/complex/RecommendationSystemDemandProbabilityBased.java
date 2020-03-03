
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.PastRecommendations;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.types.UserUninformed;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public abstract class RecommendationSystemDemandProbabilityBased extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters{
        public double probabilityUsersObey = 1D;
        public boolean takeintoaccountexpected = true;
        public boolean takeintoaccountcompromised = true;
        public int additionalResourcesDesiredInProbability=0;
    }
    protected UtilitiesProbabilityCalculator probutils;

    protected RecommendationParameters parameters;
    public RecommendationSystemDemandProbabilityBased(JsonObject recomenderdef, SimulationServices ss, RecommendationParameters parameters) throws Exception {
        super(recomenderdef,ss, parameters);
        this.parameters=(RecommendationParameters)super.parameters;
        probutils=new UtilitiesProbabilityCalculationQueue(getDemandManager(), pastRecomendations, parameters.probabilityUsersObey,
                 parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised, parameters.additionalResourcesDesiredInProbability);
    }


    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint currentposition, double maxdist) {
        List<Recommendation> result;
        List<StationUtilityData> candidatestations = getCandidateStationsRentOrderedByDistance(currentposition,maxdist);
        //now do the specific calculation to get the final list of recommended stations
        List<StationUtilityData> su = specificOrderStationsRent(candidatestations,stationManager.consultStations(), currentposition, maxdist);
       
        if (!su.isEmpty()) {
            if (printHints) {
                printRecomendations(su, true);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbabilityTake());
                return r;
            }
            ).collect(Collectors.toList());
        } else {
            result = new ArrayList<>(0);
        }
        return result;
    }
    
    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result;
        List<StationUtilityData> candidatestations = getCandidateStationsReturnOrderedByDistance(destination, currentposition);
        
        //now do the specific calculation to get the final list of recommended stations
        List<StationUtilityData> su = specificOrderStationsReturn(candidatestations,stationManager.consultStations(), currentposition, destination);

        if (!su.isEmpty()) {
            if (printHints) {
                printRecomendations(su, false);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbabilityReturn());
                return r;
            }
            ).collect(Collectors.toList());
        } else {
            result = new ArrayList<>(0);
        }
        return result;
    }

    //the list of stations is ordered by distance to currentposition
    private List<StationUtilityData> getCandidateStationsRentOrderedByDistance(GeoPoint currentposition, double maxdistance) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double dist = currentposition.eucleadeanDistanceTo(s.getPosition());
            if (dist<=maxdistance) {
                dist = graphManager.estimateDistance(currentposition, s.getPosition() ,"foot");
                if (dist<=maxdistance) {
                    double offtime = (dist / parameters.expectedWalkingVelocity);
                    StationUtilityData sd = new StationUtilityData(s);
                    sd.setWalkTime(offtime).setWalkdist(dist).setCapacity(s.getCapacity());
                    sd.setProbabilityTake(probutils.calculateTakeProbability(sd.getStation(), sd.getWalkTime()));
                    temp.add(sd);
                }
            }
        }
        return temp.stream().sorted(rentByDistance()).collect(Collectors.toList());
    }

    //the list of stations is ordered by distance to destination
    private List<StationUtilityData> getCandidateStationsReturnOrderedByDistance(GeoPoint destination, GeoPoint currentposition) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            StationUtilityData sd = new StationUtilityData(s);
            double bikedist = graphManager.estimateDistance(currentposition, s.getPosition() ,"bike");
            double biketime = bikedist / parameters.expectedCyclingVelocity;
            double walkdist = graphManager.estimateDistance(s.getPosition(),destination ,"foot");
            double walktime =  walkdist / parameters.expectedWalkingVelocity;
            sd.setWalkTime(walktime).setWalkdist(walkdist)
                    .setCapacity(s.getCapacity())
                    .setBikedist(bikedist).setBiketime(biketime);
            sd.setProbabilityReturn(probutils.calculateReturnProbability(sd.getStation(), sd.getBiketime()));
            temp.add(sd);
        }
        return temp.stream().sorted(returnByTime()).collect(Collectors.toList());
    }

    private static Comparator<StationUtilityData> rentByDistance() {
        return (s1, s2) -> Double.compare(s1.getWalkdist(), s2.getWalkdist());
    }
    private static Comparator<StationUtilityData> returnByTime() {
        return (s1, s2) -> Double.compare(s1.getWalkTime()+s1.getBiketime(), s2.getWalkTime()+s2.getBiketime());
    }

    abstract protected  List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance);
    abstract protected  List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination );   
    
    //methods for ordering the StationUtilityData should be true if the first data should be recomended befor the second
    //take into account that distance newSD >= distance oldSD
    abstract protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD);
    abstract protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD);

    protected void addrent(StationUtilityData newSD, List<StationUtilityData> temp, double maxdistance) {
        int i = 0;
        for (; i < temp.size(); i++) {
            StationUtilityData oldSD=temp.get(i);
            if (newSD.getWalkdist() <= maxdistance && oldSD.getWalkdist() > maxdistance)  break;
            if (newSD.getWalkdist() > maxdistance && oldSD.getWalkdist() <= maxdistance)  continue;
            if (betterOrSameRent(newSD, oldSD)) {
                break;
            }
        }
        temp.add(i, newSD);
    }

    protected void addreturn(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameReturn(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }
    
    private int lowprobs = 0;
    private double probsr = 0D;
    private int callsr = 0;
    private double probst = 0D;
    private int callst = 0;
    private double avcost=0;
    private double avcosr=0;
    private double avabandorate=0;

    void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
            int max = Math.min(3, su.size());
       //     if (su.get(0).getStation().getId()==8) max=173;
       //     else return;
             if (take) {
                System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
                probst += su.get(0).getProbabilityTake();
                avcost = ((avcost*callst)+su.get(0).getExpectedtimeIfNotAbandon())/(double)(callst+1);
                avabandorate+=su.get(0).getAbandonProbability();
                callst++;
                System.out.format("Expected successrate take: %9.8f abandon rate % 9.8f expected cost: %5.1f %n", (probst / callst), (avabandorate / callst), avcost);

                if (su.get(0).getProbabilityTake() < 0.6) {
                    System.out.format("[Info] LOW PROB Take %9.8f %n", su.get(0).getProbabilityTake());
                    lowprobs++;
                }
                System.out.println("             id av ca   wtime    prob   totcost   exptime expabandon expunsucces tcostdiff  rcostdiff  aux   bestn timetobn bnprob ");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %6.5f %9.2f %9.2f    %6.5f     %6.4f %9.2f  %9.2f %9.2f",
                            i+1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getProbabilityTake(),
                            s.getTotalCost(),
                            s.getExpectedtimeIfNotAbandon(),
                            s.getAbandonProbability(),
                            s.getExpectedUnsucesses(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff(),
                            s.aux
                            );
                    StationUtilityData bn=s.bestNeighbour;
                    if (bn!=null){
                        double distto=graphManager.estimateDistance(s.getStation().getPosition(), bn.getStation().getPosition() ,"foot");
                        double timeto= (distto / parameters.expectedWalkingVelocity);
                        System.out.format(" %3d %7.1f %6.5f %n",
                            bn.getStation().getId(),
                            timeto,
                            bn.getProbabilityTake());     
                    } else {
                        System.out.println("");
                    }
                }
            } else {
                System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
                probsr += su.get(0).getProbabilityReturn();
                avcosr = ((avcosr*callsr)+su.get(0).getExpectedtimeIfNotAbandon())/(double)(callsr+1);
                callsr++;
                System.out.format("Expected successrate return: %9.8f expected cost: %5.1f %n", (probsr / callsr), avcosr);

                if (su.get(0).getProbabilityReturn() < 0.6) {
                    System.out.format("[Info] LOW PROB Return %9.8f %n", su.get(0).getProbabilityReturn());
                    lowprobs++;
                }
                System.out.println("             id av ca   wtime   btime    prob   totcost   exptime expabandon expunsucces tcostdiff  rcostdiff  aux   bestn timetobn bnwt bnprob ");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %7.1f %6.5f %9.2f %9.2f %6.5f %6.4f %9.2f %9.2f %9.2f",
                            i+1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getBiketime(),
                            s.getProbabilityReturn(),
                            s.getTotalCost(),
                            s.getExpectedtimeIfNotAbandon(),
                            s.getAbandonProbability(),
                            s.getExpectedUnsucesses(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff(),
                            s.aux);
                    StationUtilityData bn=s.bestNeighbour;
                    if (bn!=null){
                        double distto=graphManager.estimateDistance(s.getStation().getPosition(), bn.getStation().getPosition() ,"bike");
                        double timeto= (distto / parameters.expectedCyclingVelocity);
                        System.out.format(" %3d %7.1f %7.1f %6.5f %n",
                            bn.getStation().getId(),
                            timeto,
                            bn.getWalkTime(),
                            bn.getProbabilityReturn());     
                    } else {
                        System.out.println("");
                    }
                }
            }
        System.out.println();
        }
    }

}
