package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.RecommendationSystem;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphHopperIntegration;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

import java.io.IOException;
import java.util.ArrayList;
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
     * It provides methods to recommend a user a set of destination stantions. 
     */
    private RecommendationSystem recommendationSystem;
    
    public SystemManager(SimulationConfiguration simulationConfiguration) throws IOException {
        this.stations = new ArrayList<>(simulationConfiguration.getStations());
        this.bikes = stations.stream().map(Station::getBikes).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        this.reservations = new ArrayList<>();
        this.graphManager = createGraphManager(simulationConfiguration);
        this.random = SimulationRandom.getGeneralInstance();
        this.bbox = simulationConfiguration.getBoundingBox();
        this.recommendationSystem = new RecommendationSystem(graphManager);	
    }
    
    private GraphHopperIntegration createGraphManager(SimulationConfiguration simulationConfiguration) throws IOException {
    	return new GraphHopperIntegration(simulationConfiguration.getMap());
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
        return reservations.stream().filter(reservation -> reservation.getUser() == user).collect(Collectors.toList());
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
	
	public RecommendationSystem getRecommendationSystem() {
		return recommendationSystem;
	}

	/**
	 * It obtains the stations for which a user has tried to make a bike reservation in an specific moment.
	 * @param user it is the user who has tried to reserve a bike.
	 * @param timeInstant it is the moment at which he has decided he wants to reserve a bike
	 * and he has been trying it.
	 * @return a list of stations for which the bike reservation has failed because of unavailable bikes.
	 */
    public List<Station> consultStationsWithBikeReservationAttempt(User user, int timeInstant) {
        return consultReservations(user).stream()
                .filter(reservation -> reservation.getType() == ReservationType.BIKE)
                .filter(reservation -> reservation.getState() == ReservationState.FAILED)
                .filter(reservation -> reservation.getStartInstant() == timeInstant)
                .map(Reservation::getStation)
                .collect(Collectors.toList());
    }

	/**
	 * It obtains the stations for which a user hasn't tried to make a bike reservation in an specific moment.
	 * @param user it is used to find out for which stations this user hasn't tried to
	 * reserve a bike.
	 * @param timeInstant it is the moment at which he has decided he wants to reserve a bike
	 * and he has been tring it.
	 * @return a list of stations for which user hasn't still tried to reserve a bike at that specific moment.
	 */
    public List<Station> consultStationsWithoutBikeReservationAttempt(User user, int timeInstant) {
        List<Station> filteredStations = new ArrayList<>(this.stations);
        filteredStations.removeAll(consultStationsWithBikeReservationAttempt(user, timeInstant));
        return filteredStations;
    }

    public List<Station> consultStationsWithSlotReservationAttempt(User user, int timeInstant) {
        return consultReservations(user).stream()
                .filter(reservation -> reservation.getType() == ReservationType.SLOT)
                .filter(reservation -> reservation.getState() == ReservationState.FAILED)
                .filter(reservation -> reservation.getStartInstant() == timeInstant)
                .map(Reservation::getStation)
                .collect(Collectors.toList());
    }

    public List<Station> consultStationsWithoutSlotReservationAttempt(User user, int timeInstant) {
        List<Station> filteredStations = new ArrayList<>(this.stations);
        filteredStations.removeAll(consultStationsWithSlotReservationAttempt(user, timeInstant));
        return filteredStations;
    }


    public GeoPoint generateBoundingBoxRandomPoint(SimulationRandom random) {
    	return bbox.randomPoint(random);
    }
    
}
