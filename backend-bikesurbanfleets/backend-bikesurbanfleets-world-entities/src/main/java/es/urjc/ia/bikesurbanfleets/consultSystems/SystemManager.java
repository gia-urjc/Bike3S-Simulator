package es.urjc.ia.bikesurbanfleets.consultSystems;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphHopperIntegration;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Bike;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Reservation.ReservationState;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Reservation.ReservationType;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByDistance;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class contains all the information of all the entities at the system.
 * It provides all the usable methods by the user at the system.
 * @author IAgroup
 */
public class SystemManager {

    /**
     * These are all the stations at the system.
     */
    private List<Station> stations;
    
    /**
     * These are all the bikes from all stations at the system.
     */
    private List<Bike> bikes;
    
    /**
     * These are all the bike and slot reservations (and reservation attempts) of all the users at the system.
     */
    private List<Reservation> reservations;
    
    /**
     * It provides the neccesary methods to manage a graph.
     */
    private GraphManager graphManager;
    
    /**
     * It is a global random instance with an specific seed.
     */
    private SimulationRandom random;
    
    /**
     * It represents the map area where simulation is taking place.
     */
    private BoundingBox bbox;
    
    /**
     * It  indicates if the recommendation system recommends by linear distance or by 
     * real distance (the distance of the shortest route).
     */
    private boolean linearDistance;
    
    public SystemManager(List<Station> stations, String mapPath, BoundingBox bbox, boolean linearDistance) throws IOException {
        this.stations = new ArrayList<>(stations);
        this.bikes = stations.stream().map(Station::getBikes).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        this.reservations = new ArrayList<>();
        this.graphManager = createGraphManager(mapPath);
        this.random = SimulationRandom.getGeneralInstance();
        this.bbox = bbox;
        this.linearDistance = linearDistance;
    }

    private GraphHopperIntegration createGraphManager(String mapPath) throws IOException {
        return new GraphHopperIntegration(mapPath);
    }
    
    /**
     * It registers a user bike or slot reservation, in any state, in system reservation information.
     * @param reservation: it is the reservation which we want to save.
     */
    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    /**
     * It obtains all the bike and slot reservations user has gotten to make, including
     * those that have expired because of reservation timeout, and reservations which
     * user has tried to make but he hasn't been able because there weren't available bikes or slots.
     * @param user: it is the user whose reservations want to be consulted
     * @return a list of all the bike and slot reservations which the specified user
     * has makde and has tried to made.
     */
    public List<Reservation> consultReservations(User user) {
        return reservations.stream().filter(reservation -> reservation.getUser().getId() == user.getId()).collect(Collectors.toList());
    }

    public List<Station> consultStations() {
        return stations;
    }
    
    public GraphManager getGraphManager() {
        return graphManager;
    }
    
    public SimulationRandom getRandom() {
        return random;
    }
    
    public List<Station> consultOrderedStationsByDistance(User user) {
        Comparator<Station> byDistance = new ComparatorByDistance(user.getPosition());
        List<Station> stations = new ArrayList<>(this.stations);
        return stations.stream().sorted(byDistance).collect(Collectors.toList());
    }

    public GeoPoint generateBoundingBoxRandomPoint(SimulationRandom random) {
        return bbox.randomPoint(random);
    }
    
}
