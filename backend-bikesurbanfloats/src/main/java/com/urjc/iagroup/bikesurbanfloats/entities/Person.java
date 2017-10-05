package com.urjc.iagroup.bikesurbanfloats.entities;

import com.sun.istack.internal.NotNull;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

import javax.naming.ServiceUnavailableException;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Person extends Entity {

    private GeoPoint position;
    private Bike bike;

    private double walkingVelocity;  // meters/second
    private double cyclingVelocity;  // meters/second

    private boolean reservedBike;
    private boolean reservedSlot;


    public Person(int id, @NotNull GeoPoint position) {
        super(id);

        this.position = position;

        this.bike = null;

        // random velocity between 3km/h and 7km/h in m/s
        RandomUtil randomUtil = new RandomUtil();
        this.walkingVelocity = randomUtil.nextInt(3, 8) / 3.6;

        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = randomUtil.nextInt(10, 21) / 3.6;
        this.reservedBike = false;
        this.reservedSlot = false;
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

    public void setPosition(@NotNull GeoPoint position) {
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

    public void setReservedSlot(boolean reservedSlot) {
        this.reservedSlot = reservedSlot;
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

    public boolean removeBikeFrom(Station station) {
        if (bike != null) {
            // TODO: log warning (or throw error?)
            return false;
        }

        if (hasReservedBike())
            // first, reservation is cancelled to let a bike available at station to make sure one bike is available for take away
            cancelsBikeReservation(station);

        try {
            this.bike = station.removeBike();
        } catch (ServiceUnavailableException e) {
            return false;
        }

        return true;
    }

    public boolean returnBikeTo(Station station) {
        if (bike == null) {
            // TODO: log warning (or throw error?)
            return false;
        }

        try {
            station.returnBike(this.bike);
        } catch (ServiceUnavailableException e) {
            return false;
        }

        return true;
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
    public int timeToReach(@NotNull GeoPoint destination) {
        // time in seconds
        return (int) Math.round(position.distanceTo(destination) / getAverageVelocity());
    }

    // returns: station = null -> user leaves the system
    public Station determineStation() {
    	throw new IllegalStateException("Base person has not implemented determineStation");
    }

    // it musts call reservesBike method inside it 
    public boolean decidesToReserveBike(Station station) {
    	throw new IllegalStateException("Base person has not implemented decidesToReserveBike");
    }

    // it musts call reservesSlot method inside it 
    public boolean decidesToReserveSlot(Station station) {
    	throw new IllegalStateException("Base person has not implemented decidesToReserveSlot");
    }

    // returns: user decides where to go to to ride his bike (not to a station)
    public GeoPoint decidesNextPoint() {
    	throw new IllegalStateException("Base person has not implemented decidesNextPoint");
    }

    // returns: true -> user goes to a station; false -> user rides his bike to a site which isn't a station
    public boolean decidesToReturnBike() {
    	throw new IllegalStateException("Base person has not implemented decidesToReturnBike");
    }


    @Override
    public String toString() {
        String result = position.toString();
        result += " | Has Bike: " + hasBike();
        result += "| Walking Velocity: " + walkingVelocity;
        result += "| Cycling Velocity: " + cyclingVelocity + "\n";
        return result;
    }

}