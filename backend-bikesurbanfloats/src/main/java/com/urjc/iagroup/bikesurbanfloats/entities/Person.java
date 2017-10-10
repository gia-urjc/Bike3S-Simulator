package com.urjc.iagroup.bikesurbanfloats.entities;


import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.core.RectangleSimulation;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

import javax.naming.ServiceUnavailableException;

public abstract class Person extends Entity implements PersonCommonBehaviour {

    private GeoPoint position;
    private Bike bike;

    private double walkingVelocity;  // meters/second
    private double cyclingVelocity;  // meters/second
    private boolean reservedBike;
    private boolean reservedSlot;
    private Station destinationStation;
    
    protected final RandomUtil random = SystemInfo.random;
    protected final RectangleSimulation rectangle = SystemInfo.rectangle;
   

    public Person(int id, GeoPoint position) {
        super(id);

        this.position = position;
        this.bike = null;
        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = random.nextInt(3, 8) / 3.6;
        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = random.nextInt(10, 21) / 3.6;
        this.reservedBike = false;
        this.reservedSlot = false;
        this.destinationStation = null;
    }
    
    public Person(Person person) {
    	super(person.getId());
    	this.position = new GeoPoint(person.position);
        
    	if(person.bike != null) this.bike = new Bike(person.bike);
        else this.bike = null;
    	
    	this.walkingVelocity = person.walkingVelocity;
    	this.cyclingVelocity = person.cyclingVelocity;
    	this.reservedBike = person.reservedBike;
    	this.reservedSlot = person.reservedSlot;
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

    public void reservesBike(Station station) {
        this.reservedBike = true;
        station.reservesBike();
    }

    public void reservesSlot(Station station) {
        this.reservedSlot = true;
        station.reservesSlot();
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
    public Double getAverageVelocity() {
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
      

    public String toString() {
        String result = "| Id: " + getId();
        result += "| Actual Position: " + position.toString();
        result += " | Has Bike: " + hasBike();
        result += " | Actual velocity: " + getAverageVelocity();
        return result;
    }
}