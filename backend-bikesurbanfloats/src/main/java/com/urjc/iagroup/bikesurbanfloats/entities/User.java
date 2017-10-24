package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.*; 
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

/**
 * This is the main entity of the system
 * It represents the basic behaviour of all users type which can apper in the system
 * It provides an implementation for basic methods which manage common information for all kind of users
 * It provides a behaviour pattern (make decissions) which depends on specific user type properties  
 * @author IAgroup
 *
 */

public abstract class User implements Entity, UserModel<Bike, Station, Reservation> {

	public enum UserType {
        USER_TYPE
	}

    private int id;

    private GeoPoint position;
    private Bike bike;

    private double walkingVelocity;  // meters/second
    private double cyclingVelocity;  // meters/second
    
    private boolean reservedBike;
    private boolean reservedSlot;
    
    private Station destinationStation;
    private Reservation lastReservation;
    private List<Reservation> reservations;
    
    protected SystemConfiguration systemConfig;
   
    public User(int id, GeoPoint position, SystemConfiguration systemConfig) {
        this.id = id;

        this.position = position;
        this.bike = null;
        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = StaticRandom.nextInt(3, 8) / 3.6;
        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = StaticRandom.nextInt(10, 21) / 3.6;
        this.reservations = new ArrayList<>();
        this.lastReservation = null;
        this.reservedBike = false;
        this.reservedSlot = false;
        this.destinationStation = null;
        this.systemConfig = systemConfig;
    }
    
    public List<Reservation> getReservations() {
    	return reservations;
    }
    
    public void addReservation(Reservation reservation) {
    	reservations.add(reservation);
    	lastReservation = reservation;
    }

    @Override
    public Reservation getLastReservation() {
        return lastReservation;
    }
    
	@Override
    public int getId() {
        return id;
    }
	
	@Override
    public GeoPoint getPosition() {
        return position;
    }

    public void setPosition(GeoPoint position) {
        this.position = position;
    }

    public void setPosition(Double latitude, Double longitude) {
        this.position.setLatitude(latitude);
        this.position.setLongitude(longitude);
    }
    
    @Override
    public Bike getBike() {
        return bike;
    }

    // TODO: is it used? we must use it
    public boolean hasBike() {
        return bike != null ? true : false;
    }

    public boolean hasReservedBike() {
        return reservedBike;
    }

    public boolean hasReservedSlot() {
        return reservedSlot;
    }
    
    /**
     *  user reserves a bike at the specified station
     * @param station is the station where user wants to make a bike reservation
     * @return true if user is able to reserve a bike at that station (there are available bikes)
     */

    public Bike reservesBike(Station station) {
    	Bike bike = null;
    	if (station.availableBikes() > 0) {
    		this.reservedBike = true;
    		bike = station.reservesBike();
    	}
    	return bike;
    }
    

    public Station getDestinationStation() {
		return destinationStation;
	}

	public void setDestinationStation(Station destinationStation) {
		this.destinationStation = destinationStation;
	}

    /**
     * user reserves a slot at the specified station
     * @param station is the station where user wants to make a slot reservation
     * @return true if user is able to reserve a slot at that station
     */

    public boolean reservesSlot(Station station) {
    	if (station.availableSlots() > 0) {
    		this.reservedSlot = true;
    		station.reservesSlot();
    	}
    	return reservedSlot;
    }
    
    /**
     * user cancels his bike reservation at the specified station
     * @param station is station for which user wants to cancel his bike reservation  
     */

    public void cancelsBikeReservation(Station station) {
        this.reservedBike = false;
        station.cancelsBikeReservation();
    }

    /**
     * user cancels his slot reservation at the specified station
     * @param station is station for which user wants to cancel his slot reservation  
     */

    public void cancelsSlotReservation(Station station) {
        this.reservedSlot = false;
        station.cancelsSlotReservation();
    }
    
    /** 
     * user removes a bike from specified station
     * @param station is the station where he wnats to remove (rent) a bike
     * @return true if user has been able to remove a bike (there are available bikes or he has a bike reservation)
     */
	
	public boolean removeBikeFrom(Station station) {
        if (bike != null) {
            return false;
        }

        if (hasReservedBike())
            // first, reservation is cancelled to let a bike available at station to make sure one bike is available for take away
            cancelsBikeReservation(station);

        this.bike = station.removeBike();
        return bike != null;
    }
	
	/**
	 * user returns his rented bike to the specified station 
	 * @param station is the station where user wants to return his bike
	 * @return true if user has been ablo to return his bike (there available slots or he has a slot reservation)
	 */

    public boolean returnBikeTo(Station station) {
        boolean result = false;
    	if (bike == null) {
            // TODO: log warning (or throw error?)
            return false;
        }
    	
    	if (hasReservedSlot())
    		cancelsSlotReservation(station);
    	
        if(station.returnBike(this.bike)){
        	this.bike = null;
        	result = true;
        }
        
        return result;
    }

    /**
     * The user's average velocity in m/s
     * @return user walking velocity if he hasn't a bike at that moment and cycling velocity in other case
     */
    public double getAverageVelocity() {
        return bike == null ? walkingVelocity : cyclingVelocity;
    }

    /**
     * time in seconds that user takes in arriving at the new station
     * time = distance/velocity
     */
    public int timeToReach(GeoPoint destination) {
        return (int) Math.round(position.distanceTo(destination) / getAverageVelocity());
    }
    
    /**
     * Stations for which user has tried to make a bike reservation
     * @param instant is the time instant at which user has tried to reserve 
     * @return a list of stations with a bike reservation attempt
     */
    
	public List<Station> obtainStationsWithBikeReservationAttempts(int instant) {
 		List<Reservation> unsuccessfulBikeReservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.BIKE && 
 		reservation.getState() == ReservationState.FAILED && reservation.getStartInstant() == instant).collect(Collectors.toList());
 		List<Station> failedStations = unsuccessfulBikeReservations.stream().map(Reservation::getStation).collect(Collectors.toList());
 		return failedStations;
	}
	
    /**
     * Stations for which user has not tried to make a bike reservation
     * @param instant is the time instant at which user wants to know at which stations he hasn't tried to reserve until that moment  
     * @return a list of stations without a bike reservation attempt
     */

	public List<Station> obtainStationsWithoutBikeReservationAttempts(int instant) {
		List<Station> failedStations = obtainStationsWithBikeReservationAttempts(instant);
	 	List<Station> stations = new ArrayList<>(systemConfig.getStations());
	 	if (!failedStations.isEmpty()) {
	 		for (Station station: failedStations) {
	 			stations.remove(station);
	 		}
	 	}
		return stations;
	}
	
    /**
     * Stations for which user has tried to make a slot reservation
     * @param instant is the time instant at which user has tried to reserve 
     * @return a list of stations with a slot reservation attempt
     */

	public List<Station> obtainStationsWithSlotReservationAttempts(int instant) {
 		List<Reservation> unsuccessfulSlotReservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.SLOT && 
 				reservation.getState() == ReservationState.FAILED && reservation.getStartInstant() == instant).collect(Collectors.toList());
 		List<Station> failedStations = unsuccessfulSlotReservations.stream().map(Reservation::getStation).collect(Collectors.toList());
 		return failedStations;
	}
	
	/**
     * Stations for which user has not tried to make a slot reservation
     * @param instant is the time instant at which user wants to know at which stations he hasn't tried to reserve until that moment  
     * @return a list of stations without a slot reservation attempt
     */
	
	public List<Station> obtainStationsWithoutSlotReservationAttempts(int instant) {
		List<Station> failedStations = obtainStationsWithSlotReservationAttempts(instant);
 		List<Station> stations = new ArrayList<>(systemConfig.getStations());
 		if (!failedStations.isEmpty()) {
 			for(Station station: failedStations) {
 				stations.remove(station);
 			}
 		}
 		return stations;
	}
	
	/**
	 * User decides to leave or continue at system 
	 * @param instant is the time instant when he wants to make this decision
	 * @return true if user decides to leave the system and false in other case
	 */

    public abstract boolean decidesToLeaveSystem(int instant);
    
    /**
     * User decides to which station he wnats to go next to rent a bike
     * @param instant: is the time instant when he needs to make this decision
     * @return station where user has decided to go
     */
    
    public abstract Station determineStationToRentBike(int instant);

    /**
     * User decides to which station he wnats to go next to return his bike
     * @param instant is the time instant when he needs to make this decision
     * @return station where user has decided to go
     */

    public abstract Station determineStationToReturnBike(int instant);
    
    public abstract boolean decidesToReserveBike();
    
    public abstract boolean decidesToReserveSlot(); // must call reservesSlot method inside it
    
    /**
     * User decides the point (it is not a station) to which he wants to cycle the rented bike just after removing it from station
     * @return the point where he wants to go after making his decision
     */
    
    public abstract GeoPoint decidesNextPoint();
    
    /**
     * User decides if he'll rides the bike to a station in order to return it or if rides it to another point before returning it
     * @return true if user decides to cycle directly to a station in order to return his bike and false in other case 
     */
    
    public abstract boolean decidesToReturnBike(); // returns: true -> user goes to a station; false -> user rides his bike to a site which isn't a station
    
    /**
     * When user is going to a station and timeout happens, it calculates how far has he gotten
     * @param time: it is the period while user is walking or cycling to the destination station    
     */
    
    public abstract void updatePosition(int time);
    
    /**
     * When timeout happens or user hasn't been able to make a reservation at the destination station, he decides to continue going to that chosen station or to go to another one 
     * @return true if user chooses a new station to go and false if he continues to the previously chosen one 
     */
    
    // TODO: add this method when timeout happens
    public abstract boolean decidesToDetermineOtherStation();
    
    @Override
    public String toString() {
        String result = "| Id: " + getId();
        result += "| Actual Position: " + position.toString();
        result += " | Has Bike: " + hasBike();
        result += " | Actual velocity: " + getAverageVelocity();
        result += 	" | Has reserved bike: "+hasReservedBike();
        result += " | Has reserved slot: "+hasReservedSlot()+"\n";
        return result;
    }
}