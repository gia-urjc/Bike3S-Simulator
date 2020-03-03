package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getAllFields;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.defaultConfiguration.GlobalConfigurationParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.StationComparator;
import es.urjc.ia.bikesurbanfleets.services.demandManager.DemandManager;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;

public abstract class RecommendationSystem {

    private int minNumberRecommendations = 10;

    //variable to print debug output for analysis
    protected final boolean printHints = true;

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
        //Later the velocities are adjusted to straight line velocities
        //given a straight line distance d, the real distance dr may be estimated  
        // as dr=f*d, whewre f will be between 1 and sqrt(2) (if triangle).
        // here we consider f=1.4
        //to translate velocities from realdistances to straight line distances:
        // Vel_straightline=(d/dr)*vel_real -> Vel_straightline=vel_real/f
        //assuming real velocities of 1.1 m/s and 4 m/s for walking and biking (aprox. to 4 and 14,4 km/h)
        //the adapted straight line velocities are: 0.786m/s and 2.86m/s
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
    }

    protected RecommendationParameters parameters;
    protected PastRecommendations pastRecomendations;

    public RecommendationSystem(JsonObject recomenderdef, SimulationServices simulationServices, RecommendationParameters parameters) throws Exception {
        stationManager = simulationServices.getStationManager();
        demandManager = simulationServices.getDemandManager();
        graphManager = simulationServices.getGraphManager();
        getParameters(recomenderdef, parameters);
        this.parameters = parameters;
        this.pastRecomendations = new PastRecommendations();
    }

    final public String getParameterString() {
        return parameters.getParameterString();
    }

    protected abstract List<Recommendation> recommendStationToRentBike(GeoPoint point, double maxdist);

    protected abstract List<Recommendation> recommendStationToReturnBike(GeoPoint currentposition, GeoPoint destination);

    // the methods for getting station recomendations for renting and returning
    // given currentposition, and destination (if return) and the maximal desired distance from currentposition if rental and from destination if return
    public List<Recommendation> getRecomendedStationsToRentBike(GeoPoint currentposition, double maxdist) {
        List<Recommendation> rec = recommendStationToRentBike(currentposition, maxdist);
        if (rec.size() < minNumberRecommendations) {
            if (rec.size() == 0) {
                System.out.println("[Warn] no recommentadtions for renting at " + maxdist + "meters. Adding the closest stations with bikes to fill.  Time:" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
            }
            addAlternativeRecomendations(currentposition, rec, true);
        } else { //add expected change to station
            Recommendation first = rec.get(0);
            double timetoreach = (graphManager.estimateDistance(currentposition, first.getStation().getPosition(), "foot")
                    / parameters.expectedWalkingVelocity);
            pastRecomendations.addExpectedBikechange(first.getStation().getId(), (int) timetoreach, true);
        }
        return rec;
    }

    public List<Recommendation> getRecomendedStationsToReturnBike(GeoPoint currentposition, GeoPoint destination) {
        List<Recommendation> rec = recommendStationToReturnBike(currentposition, destination);
        if (rec.size() < minNumberRecommendations) {
            if (rec.size() == 0) {
                System.out.println("[Warn] no recommentadtions for returning. Adding the closest stations with slots to fill.  Time:" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
            }
            addAlternativeRecomendations(destination, rec, false);
        } else { //add expected change to station
            Recommendation first = rec.get(0);
            double timetoreach = (graphManager.estimateDistance(currentposition, first.getStation().getPosition(), "bike")
                    / parameters.expectedWalkingVelocity);
            pastRecomendations.addExpectedBikechange(first.getStation().getId(), (int) timetoreach, true);
        }
        return rec;
    }

    private boolean containsStation(List<Recommendation> recs, Station s) {
        if (recs.stream().anyMatch((r) -> (r.getStation() == s))) {
            return true;
        }
        return false;
    }

    private void addAlternativeRecomendations(GeoPoint point, List<Recommendation> recs, boolean take) {
        if (recs == null) {
            recs = new ArrayList<>();
        }
        int numrecsrequired = minNumberRecommendations - recs.size();
        if (numrecsrequired > 0) {
            int i = 0;
            Comparator<Station> byDistance = StationComparator.byDistance(point, graphManager, "foot");
            List<Station> temp1;
            if (take) {
                temp1 = stationsWithBikes();
            } else {
                temp1 = stationsWithSlots();
            }
            List<Station> temp = temp1.stream().sorted(byDistance).collect(Collectors.toList());
            while (numrecsrequired > 0 && i < temp.size()) {
                Station s = temp.get(i);
                if (!containsStation(recs, s)) {
                    recs.add(new Recommendation(s, null));
                    numrecsrequired--;
                }
                i++;
            }
        }
    }

    //auxiliary function to normalize values in a linear way to the range [0,1]
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

    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    protected static List<Station> stationsWithBikes() {
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

    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    protected static List<Station> stationsWithSlots() {
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

}
