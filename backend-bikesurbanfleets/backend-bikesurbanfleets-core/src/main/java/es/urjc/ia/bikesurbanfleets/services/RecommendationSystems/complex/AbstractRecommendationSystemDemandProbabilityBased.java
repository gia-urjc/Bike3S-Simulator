package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
public abstract class AbstractRecommendationSystemDemandProbabilityBased extends RecommendationSystem {

    public static class RecommendationParameters extends RecommendationSystem.RecommendationParameters {

        public double probabilityUsersObey = 1D;
        public boolean takeintoaccountexpected = true;
        public boolean takeintoaccountcompromised = true;
        public int additionalResourcesDesiredInProbability = 0;
        public double probabilityExponent = 1D;
    }
    protected UtilitiesProbabilityCalculator probutils;

    protected RecommendationParameters parameters;

    public AbstractRecommendationSystemDemandProbabilityBased(JsonObject recomenderdef, SimulationServices ss, RecommendationParameters parameters) throws Exception {
        super(recomenderdef, ss, parameters);
        this.parameters = (RecommendationParameters) super.parameters;
        probutils = new UtilitiesProbabilityCalculationQueue(parameters.probabilityExponent, getDemandManager(), pastRecomendations, parameters.probabilityUsersObey,
                parameters.takeintoaccountexpected, parameters.takeintoaccountcompromised, parameters.additionalResourcesDesiredInProbability);
    }

    @Override
    public Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint currentposition, double maxdist) {
        Stream<StationData> stationDataStream = candidates
          //      .filter(s -> s.availableBikes>0) //no filter; all stations are used
                .map(sd -> {
                    sd.probabilityTake = probutils.calculateTakeProbability(sd.station, sd.walktime);
                    return sd;
                })//calculate the getting probability
                .sorted(rentByTime()); //sort by time to the station
        //get the specific order of the recommender
        return specificOrderStationsRent(stationDataStream, stationManager.consultStations(), currentposition, maxdist);
    }

    @Override
    public Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination) {
        Stream<StationData> stationDataStream = candidates
        //        .filter(s -> s.availableSlots>0) //no filter; all stations are used
                .map(sd -> {
                    sd.probabilityReturn = probutils.calculateReturnProbability(sd.station, sd.biketime);
                    return sd;
                })//calculate the getting probability
                .sorted(returnByTime()); //sort by time to the station and destination
        //get the specific order of the recommender
        return specificOrderStationsReturn(stationDataStream, stationManager.consultStations(), currentposition, destination);
    }

    //comparators to be used by the recommenders
    protected static Comparator<StationData> costRentComparator(double desiarableProbability) {
        return (newSD, oldSD) -> {
            /*       if (newSD.probabilityTake >= desiarableProbability
                    && oldSD.probabilityTake < desiarableProbability) {
                return -1;
            }
            if (newSD.probabilityTake < desiarableProbability
                    && oldSD.probabilityTake >= desiarableProbability) {
                return 1;
            }*/
            return Double.compare(newSD.totalCost, oldSD.totalCost);
        };
    }

    protected static Comparator<StationData> costReturnComparator(double desiarableProbability) {
        return (newSD, oldSD) -> {
            /*     if (newSD.probabilityReturn >= desiarableProbability
                    && oldSD.probabilityReturn < desiarableProbability) {
                return -1;
            }
            if (newSD.probabilityReturn < desiarableProbability
                    && oldSD.probabilityReturn >= desiarableProbability) {
                return 1;
            }
             */
            return Double.compare(newSD.totalCost, oldSD.totalCost);
        };
    }

    private static Comparator<StationData> rentByTime() {
        return (s1, s2) -> Double.compare(s1.walktime, s2.walktime);
    }

    private static Comparator<StationData> returnByTime() {
        return (s1, s2) -> Double.compare(s1.walktime + s1.biketime, s2.walktime + s2.biketime);
    }

    abstract protected Stream<StationData> specificOrderStationsRent(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance);

    abstract protected Stream<StationData> specificOrderStationsReturn(Stream<StationData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination);

    private int lowprobs = 0;
    private double probsr = 0D;
    private int callsr = 0;
    private double probst = 0D;
    private int callst = 0;
    private double avcost = 0;
    private double avcosr = 0;
    private double avabandorate = 0;

    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        if (rentbike) {
            //statistics
            StationData fistr = su.get(0);
            probst += fistr.probabilityTake;
            avcost = ((avcost * callst) + fistr.walktime) / (double) (callst + 1);
            avabandorate += fistr.abandonProbability;
            callst++;
            System.out.format("Expected successrate take: %9.8f abandon rate % 9.8f expected time (if sucess): %5.1f %n", (probst / callst), (avabandorate / callst), avcost);
            if (fistr.probabilityTake < 0.6) {
                System.out.format("[Info] LOW PROB Take %9.8f %n", fistr.probabilityTake);
                lowprobs++;
            }

            //the reccommendations
            System.out.println("             id av ca    wtime      prob  exptime   indcost   totcost  tcostdiff  rcostdiff    bestn timetobn bnprob ");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f   %6.5f %8.2f %9.2f %9.2f  %9.2f  %9.2f ",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.probabilityTake,
                        s.expectedTimeIfNotAbandon,
                        s.individualCost,
                        s.totalCost,
                        s.takecostdiff,
                        s.returncostdiff
                );
                Station bn = s.bestNeighbour;
                if (bn != null) {
                    double distto = graphManager.estimateDistance(s.station.getPosition(), bn.getPosition(), "foot");
                    double timeto = (distto / parameters.expectedWalkingVelocity);
                    System.out.format(" %3d %7.1f %6.5f %n",
                            bn.getId(),
                            timeto,
                            s.bestNeighbourProbability);
                } else {
                    System.out.println("");
                }
                i++;
            }
        } else {
            //statistics
            StationData fistr = su.get(0);
            probsr += fistr.probabilityReturn;
            avcosr = ((avcosr * callsr) + fistr.walktime+ fistr.biketime) / (double) (callsr + 1);
            callsr++;
            System.out.format("Expected successrate return: %9.8f expected time (if sucess): %5.1f %n", (probsr / callsr), avcosr);
            if (fistr.probabilityReturn < 0.6) {
                System.out.format("[Info] LOW PROB Return %9.8f %n", fistr.probabilityReturn);
                lowprobs++;
            }

            //the reccommendations
            System.out.println("             id av ca    wtime    btime    prob  exptime   indcost    totcost  tcostdiff  rcostdiff    bestn timetobn bnwt bnprob ");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f %6.5f %8.2f %9.2f  %9.2f  %9.2f  %9.2f ",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.biketime,
                        s.probabilityReturn,
                        s.expectedTimeIfNotAbandon,
                        s.individualCost,
                        s.totalCost,
                        s.takecostdiff,
                        s.returncostdiff);
                Station bn = s.bestNeighbour;
                if (bn != null) {
                    double distto = graphManager.estimateDistance(s.station.getPosition(), bn.getPosition(), "bike");
                    double timeto = (distto / parameters.expectedCyclingVelocity);
                    System.out.format(" %3d %7.1f %7.1f %6.5f %n",
                            bn.getId(),
                            timeto,
                            s.bestNeighbourReturnWalktime,
                            s.bestNeighbourProbability);
                } else {
                    System.out.println("");
                }
                i++;
            }
        }
        System.out.println();
    }
}
