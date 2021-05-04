package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems;

import es.urjc.ia.bikesurbanfleets.services.Recommendation;
import com.google.gson.JsonObject;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getAllFields;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RecommendationSystem {

    //variable to print debug output for analysis
    protected final boolean printHints = true;
    protected final int maxNumberPrint = 3;

    /**
     * It provides information about the infraestructure state.
     */
    protected static StationManager stationManager;
    protected static DemandManager demandManager;
    protected static GraphManager graphManager;

    public DemandManager getDemandManager() {
        return demandManager;
    }

    public static class RecommendationParameters {

        // the velocities here are real (estimated velocities)
        // assuming real velocities of 1.1 m/s and 4 m/s for walking and biking (aprox. to 4 and 14,4 km/h)
        // a factor may be applied depending on the used route estimator (graphManager)
        // this is necesary to simulate real velocitis when estimation with straight line distances
        public double expectedWalkingVelocity = GlobalConfigurationParameters.DEFAULT_WALKING_VELOCITY;
        public double expectedCyclingVelocity = GlobalConfigurationParameters.DEFAULT_CYCLING_VELOCITY;

        public String getParameterString() {
            String s = " ";
            List<Field> F = getAllFields(this.getClass());
            for (Field f : F) {
                try {
                    f.setAccessible(true);
                    s = s + f.getName() + "=" + f.get(this) + ", ";
                } catch (Exception ex) {
                    throw new RuntimeException("Error in writing parameters");
                }
            }
            return s;
            //          return " expectedwalkingVelocity=" + expectedWalkingVelocity + ", expectedcyclingVelocity=" + expectedCyclingVelocity  ;
        }

        void updateVelocities() {
            expectedWalkingVelocity = expectedWalkingVelocity * graphManager.getVelocityFactor();
            expectedCyclingVelocity = expectedCyclingVelocity * graphManager.getVelocityFactor();;
        }
    }

    protected RecommendationParameters parameters;
    protected PastRecommendations pastRecomendations;

    public RecommendationSystem(JsonObject recomenderdef, SimulationServices simulationServices, RecommendationParameters parameters) throws Exception {
        stationManager = simulationServices.getStationManager();
        demandManager = simulationServices.getDemandManager();
        graphManager = simulationServices.getGraphManager();
        getParameters(recomenderdef, parameters);
        this.parameters = parameters;
        this.parameters.updateVelocities();
        this.pastRecomendations = new PastRecommendations();
    }

    final public String getParameterString() {
        return parameters.getParameterString();
    }

    protected abstract Stream<StationData> recommendStationToRentBike(final Stream<StationData> candidates, final GeoPoint point, double maxdist);

    protected abstract Stream<StationData> recommendStationToReturnBike(final Stream<StationData> candidates, final GeoPoint currentposition, final GeoPoint destination);

    // the public methods for getting station recomendations for renting and returning
    // this function returns a list of recommended stations (ordered by the used recommender)
    // it returns ONLY stations within maxdist from the susers current position
    // if not enough stations are recommended to get the minimum number, 
    // stations within max dist will be added
    public List<Recommendation> getRecomendedStationsToRentBike(GeoPoint currentposition, double maxdist) {
        //get the basic station candidates
        List<StationData> candidates = getCandidatesToRentBike(currentposition, maxdist);
        //filter stations where the user is and there are no bikes
        Stream<StationData> candidatestream=candidates.stream()
                .filter(stationdata -> (stationdata.walkdist>10 || stationdata.availableBikes > 0));
        //get the recomendations as stream
        List<StationData> recomendations
                = recommendStationToRentBike(candidatestream, currentposition, maxdist).
                        collect(Collectors.toList());
        //print results on console
        if (printHints) {
            printRecomendations(recomendations, maxdist, true);
        }
        //add the expected bikechanges
        //if the recommendation is empty, the user will abandon and no expected bike change is added
        if (!recomendations.isEmpty()) {
            StationData sd = recomendations.get(0);
            pastRecomendations.addExpectedBikechange(sd.station.getId(), (int) sd.walktime, true);
        }
        //return results
        return recomendations.stream().map(sq
                -> new Recommendation(sq.station, sq.walkdist, sq.bikedist, sq.probabilityTake, null))
                .collect(Collectors.toList());
    }

    public List<Recommendation> getRecomendedStationsToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        //get the basic station candidates
        List<StationData> candidates = getCandidatesToReturnBike(currentposition, destination);

        //filter stations where the user is and there are no bikes
        Stream<StationData> candidatestream=candidates.stream()
                .filter(stationdata -> (stationdata.bikedist>10 || stationdata.availableSlots > 0));

        //get the recomendations as stream
        List<StationData> recomendations = recommendStationToReturnBike(candidatestream, currentposition, destination).
                collect(Collectors.toList());
        //print results on console
        if (printHints) {
            printRecomendations(recomendations, 0, false);
        }
        //add the expected bikechanges
        //if the recommendation is empty, the user will abandon and no expected bike change is added
        if (!recomendations.isEmpty()) {
            StationData sd = recomendations.get(0);
            pastRecomendations.addExpectedBikechange(sd.station.getId(), (int) sd.biketime, false);
        }
        //return results
        return recomendations.stream().map(sq
                -> new Recommendation(sq.station, sq.walkdist, sq.bikedist, sq.probabilityReturn, null))
                .collect(Collectors.toList());
    }

    // methods for getting the sation candidates and precalculation of some basic information
    // in the case of getting a bike, only stations within the maxdistance are returned
    private List<StationData> getCandidatesToRentBike(GeoPoint position, double maxdist) {
        List<StationData> candidates = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double dist = position.eucleadeanDistanceTo(s.getPosition());
            if (dist <= maxdist) {
                dist = graphManager.estimateDistance(position, s.getPosition(), "foot");
                if (dist <= maxdist) {
                    double walktime = (dist / parameters.expectedWalkingVelocity);
                    candidates.add(new StationData(s, dist, walktime));
                }
            }
        }
        return candidates;
    }

    private List<StationData> getCandidatesToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<StationData> candidates = new ArrayList<>();
        for (Station s : stationManager.consultStations()) {
            double distbike = graphManager.estimateDistance(currentposition, s.getPosition(), "bike");
            double distwalk = graphManager.estimateDistance(s.getPosition(), destination, "foot");
            double walktime = (distwalk / parameters.expectedWalkingVelocity);
            double biketime = (distbike / parameters.expectedCyclingVelocity);
            candidates.add(new StationData(s, distbike, biketime, distwalk, walktime));
        }
        return candidates;
    }

    private final void printRecomendations(List<StationData> su, double maxdist, boolean rentbike) {
        long maxnumber = Math.min(maxNumberPrint, su.size());

        if (rentbike) {
            System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
        } else {
            System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
        }
        if (su.size() < 1) {
            if (rentbike) {
                System.out.println("No recommendations found for renting at " + maxdist + "meters.");
            } else {
                System.out.println("No recommendations found for returning.");
            }
        } else {
            printRecomendationDetails(su, rentbike, maxnumber);
        }
        System.out.println();
    }

    //default implementation for printing details
    protected void printRecomendationDetails(List<StationData> su, boolean rentbike, long maxnumber) {
        if (rentbike) {
            System.out.println("             id av ca    wtime");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f%n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime);
                i++;
            }
        } else {
            System.out.println("             id av ca    wtime    btime");
            int i = 1;
            for (StationData s : su) {
                if (i > maxnumber) {
                    break;
                }
                System.out.format("%-3d Station %3d %2d %2d %8.2f %8.2f%n",
                        i,
                        s.station.getId(),
                        s.station.availableBikes(),
                        s.station.getCapacity(),
                        s.walktime,
                        s.biketime);
                i++;
            }
        }
    }

    /*    //auxiliary function to normalize values in a linear way to the range [0,1]
    protected double normatizeTo01(double value, double minvalue, double maxvalue) {
        if (maxvalue <= minvalue) {
            throw new RuntimeException("invalid program state");
        }
        if (value < minvalue) {
            throw new RuntimeException("invalid program state");
        }
        if (value > maxvalue) {
            throw new RuntimeException("invalid program state");
        }
        return (value - minvalue) / (maxvalue - minvalue);
    }
     */
    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    /*    protected static List<Station> stationsWithBikes() {
        List<Station> stations = stationManager.consultStations().stream().filter(station -> station.availableBikes() > 0)
                .collect(Collectors.toList());
        return stations;
    }

    protected static List<Station> stationsWithBikesInWalkingDistance(GeoPoint position, double maxdist) {
        List<Station> stations = stationManager.consultStations().stream()
                .filter(station -> station.availableBikes() > 0
                && graphManager.estimateDistance(position, station.getPosition(), "foot") <= maxdist)
                .collect(Collectors.toList());
        return stations;
    }
     */
    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    /*    protected static List<Station> stationsWithSlots() {
        List<Station> stations = stationManager.consultStations().stream().filter(station -> station.availableSlots() > 0)
                .collect(Collectors.toList());
        return stations;
    }

    protected static List<Station> stationsWithSlotsInWalkingDistance(GeoPoint position, double maxdist) {
        List<Station> stations = stationManager.consultStations().stream()
                .filter(station -> (station.availableSlots() > 0)
                && (graphManager.estimateDistance(station.getPosition(), position, "foot") <= maxdist))
                .collect(Collectors.toList());
        return stations;
    }
     */
}
