package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.ReservationType;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

/**
 * This is the main entity of the system
 * It represents the basic behaviour of all users type which can apper in the system
 * It provides an implementation for basic methods which manage common information for all kind of users
 * It provides a behaviour pattern (make decissions) which depends on specific user type properties  
 * @author IAgroup
 *
 */

public abstract class User implements Entity, UserModel<Bike, Station> {

    private int id;

    private GeoPoint position;
    private Bike bike;

    private double walkingVelocity;  // meters/second
    private double cyclingVelocity;  // meters/second
    
    private boolean reservedBike;
    private boolean reservedSlot;
    
    private Station destinationStation;
    private List<Reservation> reservations;
    
    protected SystemInfo systemInfo;
   
    public User(int id, GeoPoint position, SystemInfo systemInfo) {
        this.id = id;

        this.position = position;
        this.bike = null;
        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = StaticRandom.nextInt(3, 8) / 3.6;
        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = StaticRandom.nextInt(10, 21) / 3.6;
        this.reservations = new ArrayList<>();        
        this.reservedBike = false;
        this.reservedSlot = false;
        this.destinationStation = null;
        this.systemInfo = systemInfo;
    }
    
    public List<Reservation> getReservations() {
    	return reservations;
    }
    
    public void addReservation(Reservation reservation) {
    	reservations.add(reservation);
    }
    
	@Override
    public int getId() {
        return id;
    }

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

    public Bike getBike() {
        return bike;
    }

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
     *  user reserves a bike at the station
     * @param station is the station where user wants to make a bike reservation
     * @return true if user is able to reserve a bike at that station (there are available bikes)
     */

    public boolean reservesBike(Station station) {
    	if (station.availableBikes() > 0) {
    		this.reservedBike = true;
    		station.reservesBike();
    	}
    	return reservedBike;
    }
    
    /**
     * user reserves a slot at the station
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
     * user cancels his bike reservation at the station
     * @param station is station for which user wants to cancel his bike reservation  
     */

    public void cancelsBikeReservation(Station station) {
        this.reservedBike = false;
        station.cancelsBikeReservation();
    }

    public void cancelsSlotReservation(Station station) {
        this.reservedSlot = false;
        station.cancelsSlotReservation();
    }

    public Station getDestinationStation() {
		return destinationStation;
	}

	public void setDestinationStation(Station destinationStation) {
		this.destinationStation = destinationStation;
	}
	
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
     * The person's average velocity in m/s
     *
     * @return
     */
    public double getAverageVelocity() {
        return bike == null ? walkingVelocity : cyclingVelocity;
    }

    /**
     * time that user takes in arriving at the new station
     * time = distance/velocity
     */
    public int timeToReach(GeoPoint destination) {
        // time in seconds
        return (int) Math.round(position.distanceTo(destination) / getAverageVelocity());
    }
    
	public List<Station> obtainStationsWithBikeReservationAttempts(int instant) {
 		List<Reservation> unsuccessfulBikeReservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.BIKE && 
 		reservation.getSuccessful() == false && reservation.getInstant() == instant).collect(Collectors.toList());
 		List<Station> failedStations = unsuccessfulBikeReservations.stream().map(Reservation::getStation).collect(Collectors.toList());
 		return failedStations;
	}

	public List<Station> obtainStationsWithoutBikeReservationAttempts(int instant) {
		List<Station> failedStations = obtainStationsWithBikeReservationAttempts(instant);
	 	List<Station> stations = new ArrayList<>(systemInfo.stations);
	 	if (!failedStations.isEmpty()) {
	 		for (Station station: failedStations) {
	 			stations.remove(station);
	 		}
	 	}
		return stations;
	}
	
	public List<Station> obtainStationsWithSlotReservationAttempts(int instant) {
 		List<Reservation> unsuccessfulSlotReservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.SLOT && 
 				reservation.getSuccessful() == false && reservation.getInstant() == instant).collect(Collectors.toList());
 		List<Station> failedStations = unsuccessfulSlotReservations.stream().map(Reservation::getStation).collect(Collectors.toList());
 		return failedStations;
	}
	
	public List<Station> obtainStationsWithoutSlotReservationAttempts(int instant) {
		List<Station> failedStations = obtainStationsWithSlotReservationAttempts(instant);
 		List<Station> stations = new ArrayList<>(systemInfo.stations);
 		if (!failedStations.isEmpty()) {
 			for(Station station: failedStations) {
 				stations.remove(station);
 			}
 		}
 		return stations;
	}

    public abstract boolean decidesToLeaveSystem(int instant);
    public abstract Station determineStationToRentBike(int instant);
    public abstract Station determineStationToReturnBike(int instant);
    public abstract boolean decidesToReserveBike(); // must call reservesBike method inside it
    public abstract boolean decidesToReserveSlot(); // must call reservesSlot method inside it
    public abstract GeoPoint decidesNextPoint(); // returns: user decides where to go to to ride his bike (not to a station)
    public abstract boolean decidesToReturnBike(); // returns: true -> user goes to a station; false -> user rides his bike to a site which isn't a station
    public abstract void updatePosition(int time); // walked distance during a time period
    public abstract boolean decidesToRentBikeAtOtherStation();
    
    public abstract boolean decidesToDetermineOtherStation();

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