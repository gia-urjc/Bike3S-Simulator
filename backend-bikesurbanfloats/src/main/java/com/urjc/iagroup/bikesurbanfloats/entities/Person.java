package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public abstract class Person implements Entity, UserModel<Bike, Station> {

    private int id;

    private GeoPoint position;
    private Bike bike;

    private double walkingVelocity;  // meters/second
    private double cyclingVelocity;  // meters/second
    private boolean reservedBike;
    private boolean reservedSlot;
    private Station destinationStation;
    private List<Station> bikeReservationAttempts;
    private List<Station> slotReservationAttempts;
   
    public Person(int id, GeoPoint position) {
        this.id = id;

        this.position = position;
        this.bike = null;
        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = SystemInfo.random.nextInt(3, 8) / 3.6;
        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = SystemInfo.random.nextInt(10, 21) / 3.6;
        this.bikeReservationAttempts = new ArrayList<>();
        this.slotReservationAttempts = new ArrayList<>();
        this.reservedBike = false;
        this.reservedSlot = false;
        this.destinationStation = null;
    }
    
    public List<Station> getBikeReservationAttempts() {
    	return bikeReservationAttempts;
    }
    
    public List<Station> getSlotReservationAttempts() {
    	return slotReservationAttempts;
    }
    
    public void addBikeReservationAttemp(Station station) {
    	bikeReservationAttempts.add(station);
    }
    
    public void addSlotReservationAttempt(Station station) {
    	slotReservationAttempts.add(station);
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

    public boolean reservesBike(Station station) {
    	if (station.availableBikes() > 0) {
    		this.reservedBike = true;
    		station.reservesBike();
    	}
    	return reservedBike;
    }

    public boolean reservesSlot(Station station) {
    	if (station.availableSlots() > 0) {
    		this.reservedSlot = true;
    		station.reservesSlot();
    	}
    	return reservedSlot;
    }

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

    public abstract boolean decidesToLeaveSystem();
    public abstract Station determineStationToRentBike();
    public abstract Station determineStationToReturnBike();
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
        return result;
    }
}