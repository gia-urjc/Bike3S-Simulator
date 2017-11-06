package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
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
	 * All the stations at the system.
	 */
    private List<Station> stations;
    /**
     * All the bikes from all stations at the system. 
     */
    private List<Bike> bikes;
    /**
     * All the bike and slot reservations (and reservation attempts) of all the users at the system.
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

    public SystemManager(List<Station> stations, SimulationConfiguration systemConfiguration) throws IOException {
        this.stations = new ArrayList<>(stations);
        this.bikes = stations.stream().map(Station::getBikes).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        this.reservations = new ArrayList<>();
        this.graphManager = createGraphManager(systemConfiguration);
        this.random = SimulationRandom.createRandom();
        this.bbox = systemConfiguration.getBoundingBox();
    }
    
    private GraphHopperIntegration createGraphManager(SimulationConfiguration systemConfiguration) throws IOException {
    	String mapDirectory = systemConfiguration.getMapDirectory();
    	String graphhopperLocale = systemConfiguration.getGraphHopperLocale();
    	return new GraphHopperIntegration(mapDirectory, graphhopperLocale);
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    /**
     * It allow consulting all the reservations 
     * @param user: it is the user whose reservations want to be consulted
     * @return a list of all the bike and slot reservations which the specified user has makde.
     */
    public List<Reservation> consultReservations(User user) {
        return reservations.stream().filter(reservation -> reservation.getUser() == user).collect(Collectors.toList());
    }

    public List<Station> consultStations() {
        return stations;
    }

    public List<Bike> consultBikes() {
        return bikes;
    }
    
    public GraphManager getGraphManager() {
		return graphManager;
	}
	
	public SimulationRandom getRandom() {
		return random;
	}

    public List<Station> consultStationsWithBikeReservationAttempt(User user, int timeInstant) {
        return consultReservations(user).stream()
                .filter(reservation -> reservation.getType() == ReservationType.BIKE)
                .filter(reservation -> reservation.getState() == ReservationState.FAILED)
                .filter(reservation -> reservation.getStartInstant() == timeInstant)
                .map(Reservation::getStation)
                .collect(Collectors.toList());
    }

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
    
    public GeoPoint generateBoundingBoxRandomPoint() {
    	return bbox.randomPoint();
    }
    
}
