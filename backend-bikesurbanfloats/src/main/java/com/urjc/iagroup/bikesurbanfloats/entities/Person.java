package com.urjc.iagroup.bikesurbanfloats.entities;

import com.sun.istack.internal.NotNull;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import javax.naming.ServiceUnavailableException;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Person {

    private GeoPoint position;

    private Station destination;
    private Bike bike;

    private Double walkingVelocity;
    private Double cyclingVelocity;

    public Person(GeoPoint position) {
        this.position = position;

        this.destination = null;
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

    public Station getDestination() {
        return destination;
    }

    public Bike getBike() {
        return bike;
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
     * @return
     */
    public Double getAverageVelocity() {
        return bike == null ? walkingVelocity : cyclingVelocity;
    }

    // TODO: Posible
    abstract Station determineDestination();
    
}
