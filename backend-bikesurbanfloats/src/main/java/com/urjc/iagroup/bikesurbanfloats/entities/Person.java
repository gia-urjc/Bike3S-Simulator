package com.urjc.iagroup.bikesurbanfloats.entities;

import com.sun.istack.internal.NotNull;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import javax.naming.ServiceUnavailableException;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Person {

    private GeoPoint position;

    private Bike bike;

    private Double walkingVelocity;  // meters/second
    private Double cyclingVelocity;  // meters/second

    public Person(@NotNull GeoPoint position) {
        this.position = position;

        this.bike = null;

        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = ThreadLocalRandom.current().nextInt(3, 8) / 3.6;

        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = ThreadLocalRandom.current().nextInt(10, 21) / 3.6;
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

    public boolean removeBikeFrom(Station station) {
        if (bike != null) {
            // TODO: log warning (or throw error?)
            return false;
        }

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


    public abstract Station determineDestination();
    
    public abstract boolean wantsToGoDirectlyToStation();
    
    @Override
    public String toString() {
    	String result = position.toString();
    	result += " | Has Bike: " + hasBike();
    	result += "| Walking Velocity: " + walkingVelocity;
    	result += "| Cycling Velocity: " + cyclingVelocity + "\n";
    	return result;
    }
    
}