
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint.STRAIGT_LINE_FACTOR;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

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
public abstract class RecommendationSystemDemandProbabilityBased extends RecommendationSystem {

    private class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        // the velocities here are real (estimated velocities)
        // assuming real velocities of 1.4 m/s and 6 m/s for walking and biking (aprox. to 5 and 20 km/h)
        //Later the velocities are adjusted to straight line velocities
        //given a straight line distance d, the real distance dr may be estimated  
        // as dr=f*d, whewre f will be between 1 and sqrt(2) (if triangle).
        // here we consider f=1.4
        //to translate velocities from realdistances to straight line distances:
        // Vel_straightline=(d/dr)*vel_real -> Vel_straightline=vel_real/f
        //assuming real velocities of 1.4 m/s and 6 m/s for walking and biking (aprox. to 5 and 20 km/h)
        //the adapted straight line velocities are: 1m/s and 4.286m/s
        public double walkingVelocity = 1.4D;
        public double cyclingVelocity = 6D;
        public double probabilityUsersObey = 1D;
        public boolean takeintoaccountexpected = true;
        public boolean takeintoaccountcompromised = true;
        public int additionalResourcesDesiredInProbability=1;
        
         @Override
        public String toString() {
            return "additionalResourcesDesiredInProbability="+additionalResourcesDesiredInProbability+", walkingVelocity=" + walkingVelocity + ", cyclingVelocity=" + cyclingVelocity + ", probabilityUsersObey=" + probabilityUsersObey + ", takeintoaccountexpected=" + takeintoaccountexpected + ", takeintoaccountcompromised=" + takeintoaccountcompromised ;
        }
    }
    public String getParameterString(){
        return this.baseparameters.toString();
    }

    protected double straightLineWalkingVelocity ;
    protected double straightLineCyclingVelocity ;

    protected RecommendationParameters baseparameters;
    protected UtilitiesProbabilityCalculator probutils;
    private PastRecommendations pastrecs;
    
    public RecommendationSystemDemandProbabilityBased(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(ss);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.baseparameters = new RecommendationParameters();
        getParameters(recomenderdef, this.baseparameters);
        //calculate the corresponding straightline velocities
        // the distances here are straight line distances
        //given a straight line distance d, the real distance dr may be estimated  
        // as dr=f*d, whewre f will be between 1 and sqrt(2) (if triangle).
        // here we consider f=1.4
        //to translate velocities from realdistances to straight line distances:
        // Vel_straightline=(d/dr)*vel_real -> Vel_straightline=vel_real/f
        //assuming real velocities of 1.4 m/s and 6 m/s for walking and biking (aprox. to 5 and 20 km/h)
        //the adapted straight line velocities are: 1m/s and 4.286m/s
        straightLineWalkingVelocity = this.baseparameters.walkingVelocity/STRAIGT_LINE_FACTOR;
        straightLineCyclingVelocity = this.baseparameters.cyclingVelocity/STRAIGT_LINE_FACTOR;
        
        pastrecs=new PastRecommendations();
        probutils=new UtilitiesProbabilityCalculation(getDemandManager(), pastrecs, baseparameters.probabilityUsersObey,
                 baseparameters.takeintoaccountexpected, baseparameters.takeintoaccountcompromised, baseparameters.additionalResourcesDesiredInProbability);
    }

    private static Comparator<Station> byDistance(GeoPoint point) {
        return (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point), s2.getPosition().distanceTo(point));
    }

    @Override
    public List<Recommendation> recommendStationToRentBike(GeoPoint currentposition) {
        List<Recommendation> result;
        List<Station> stations = stationManager.consultStations().stream().
                sorted(byDistance(currentposition)).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getOrderedStationsRent(stations, currentposition);
            if (su.size() == 0) {
                System.out.println("ERROR take: no recommendation found with minimal parameters at Time:" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
            }
            if (printHints) {
                printRecomendations(su, true);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbabilityTake());
                return r;
            }
            ).collect(Collectors.toList());
            //add values to the expeted takes
            StationUtilityData first = su.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            pastrecs.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / straightLineWalkingVelocity), true);
        } else {
            result = new ArrayList<>();
            System.out.println("[Warn] no recommendation for take at Time:" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
        }
        return result;
    }
    
    public List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> result = new ArrayList<>();
        List<Station> stations = stationManager.consultStations().stream().
                sorted(byDistance(destination)).collect(Collectors.toList());

        if (!stations.isEmpty()) {
            List<StationUtilityData> su = getOrderedStationsReturn(stations, destination, currentposition);
            if (su.size() == 0) {
                System.out.println("[ERROR] return: no recommendation found with minimal parameters at Time:" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
            }
            if (printHints) {
                printRecomendations(su, false);
            }
            result = su.stream().map(sq -> {
                Recommendation r = new Recommendation(sq.getStation(), null);
                r.setProbability(sq.getProbabilityReturn());
                return r;
            }
            ).collect(Collectors.toList());
            //add values to the expeted returns
            StationUtilityData first = su.get(0);
            double dist = currentposition.distanceTo(first.getStation().getPosition());
            pastrecs.addExpectedBikechange(first.getStation().getId(),
                    (int) (dist / straightLineCyclingVelocity), false);
        } else {
            System.out.println("[Warn] no recommendation for return at Time:" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
        }
        return result;
    }
    private int lowprobs = 0;
    private double probsr = 0D;
    private int callsr = 0;
    private double probst = 0D;
    private int callst = 0;
    private double avcost=0;
    private double avcosr=0;

    void printRecomendations(List<StationUtilityData> su, boolean take) {
        if (printHints) {
            int max = Math.min(3, su.size());
       //     if (su.get(0).getStation().getId()==8) max=173;
       //     else return;

            if (take) {
                System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
                probst += su.get(0).getProbabilityTake();
                avcost = ((avcost*callst)+su.get(0).getTotalCost())/(double)(callst+1);
                callst++;
                System.out.format("Expected successrate take: %9.8f expected cost: %5.1f %n", (probst / callst), avcost);

                if (su.get(0).getProbabilityTake() < 0.6) {
                    System.out.format("[Info] LOW PROB Take %9.8f %n", su.get(0).getProbabilityTake());
                    lowprobs++;
                }
                System.out.println("             id av ca   wtime    prob   indcost tcostdiff  rcostdiff   totcost bestn timetobn bnwt bnprob");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %6.5f %9.2f %9.2f %9.2f %9.2f",
                            i+1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getProbabilityTake(),
                            s.getIndividualCost(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff(),
                            s.getTotalCost());
                    StationUtilityData bn=s.bestNeighbour;
                    if (bn!=null){
                        double distto=bn.getStation().getPosition().distanceTo(s.getStation().getPosition());
                        double timeto= (distto / straightLineWalkingVelocity);
                        System.out.format(" %3d %7.1f %7.1f %6.5f %n",
                            bn.getStation().getId(),
                            timeto,
                            bn.getWalkTime(),
                            bn.getProbabilityTake());     
                    } else {
                        System.out.println("");
                    }
                }
            } else {
                System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime()+ "("+SimulationDateTime.getCurrentSimulationInstant()+")");
                probsr += su.get(0).getProbabilityReturn();
                avcosr = ((avcosr*callsr)+su.get(0).getTotalCost())/(double)(callsr+1);
                callsr++;
                System.out.format("Expected successrate return: %9.8f expected cost: %5.1f %n", (probsr / callsr), avcosr);

                if (su.get(0).getProbabilityReturn() < 0.6) {
                    System.out.format("[Info] LOW PROB Return %9.8f %n", su.get(0).getProbabilityReturn());
                    lowprobs++;
                }
                System.out.println("             id av ca   wtime   btime    prob   indcost tcostdiff  rcostdiff   totcost bestn timetobn bnwt bnprob");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %7.1f %6.5f %9.2f %9.2f %9.2f %9.2f",
                            i+1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getBiketime(),
                            s.getProbabilityReturn(),
                            s.getIndividualCost(),
                            s.getTakecostdiff(),
                            s.getReturncostdiff(),
                            s.getTotalCost());
                    StationUtilityData bn=s.bestNeighbour;
                    if (bn!=null){
                        double distto=bn.getStation().getPosition().distanceTo(s.getStation().getPosition());
                        double timeto= (distto / straightLineCyclingVelocity);
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
    
    //the list of stations is ordered by distance to currentposition
    private List<StationUtilityData> getOrderedStationsRent(List<Station> stations, GeoPoint currentposition) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double dist = currentposition.distanceTo(s.getPosition());
            double offtime = (dist / straightLineWalkingVelocity);
            sd.setWalkTime(offtime).setWalkdist(dist).setCapacity(s.getCapacity());
            temp.add(sd);
        }
        List<StationUtilityData> ret=specificOrderStationsRent(temp,stations, currentposition);
        return ret;    
    }

    //the list of stations is ordered by distance to destination
    private List<StationUtilityData> getOrderedStationsReturn(List<Station> stations, GeoPoint destination, GeoPoint currentposition) {
        List<StationUtilityData> temp = new ArrayList<>();
        for (Station s : stations) {
            StationUtilityData sd = new StationUtilityData(s);
            double bikedist=currentposition.distanceTo(s.getPosition());
            double biketime = bikedist / straightLineCyclingVelocity;
            double walkdist=s.getPosition().distanceTo(destination);
            double walktime =  walkdist / straightLineWalkingVelocity;
            sd.setWalkTime(walktime).setWalkdist(walkdist)
                    .setCapacity(s.getCapacity())
                    .setBikedist(bikedist).setBiketime(biketime);
            temp.add(sd);
        }
        List<StationUtilityData> ret=specificOrderStationsReturn(temp,stations, currentposition, destination);
        return ret;    
    }

    abstract protected  List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition );
    abstract protected  List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination );   
    
    //methods for ordering the StationUtilityData should be true if the first data should be recomended befor the second
    //take into account that distance newSD >= distance oldSD
    abstract protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD);
    abstract protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD);

    protected void addrent(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameRent(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
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
}
