package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationState;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphHopperImpl;
import com.urjc.iagroup.bikesurbanfloats.graphs.GraphManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SystemManager {

    private List<Station> stations;
    private List<Bike> bikes;
    private List<Reservation> reservations;
    private GraphManager graphManager; 

    public SystemManager(List<Station> stations, SimulationConfiguration systemConfiguration) throws IOException {
        this.stations = new ArrayList<>(stations);
        this.bikes = stations.stream().map(Station::getBikes).flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());
        this.reservations = new ArrayList<>();
        createGraphManager(systemConfiguration);
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

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

	public void setGraphManager(GraphManager graphManager) {
		this.graphManager = graphManager;
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
    
    private void createGraphManager(SimulationConfiguration systemConfiguration) throws IOException {
    	String mapDirectory = systemConfiguration.getMapDirectory();
    	String graphhopperDirectory = systemConfiguration.getGraphhopperDirectory();
    	String graphhopperLocale = systemConfiguration.getGraphHopperLocale();
    	setGraphManager(new GraphHopperImpl(mapDirectory, graphhopperDirectory, graphhopperLocale));
    }
}
