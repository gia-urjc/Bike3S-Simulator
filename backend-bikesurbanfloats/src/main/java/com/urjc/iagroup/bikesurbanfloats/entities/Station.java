package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.models.StationModel;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import java.util.List;
import java.util.Objects;

public class Station implements Entity, StationModel<Bike> {

    private int id;

    private final GeoPoint position;

    private int capacity;
    private List<Bike> bikes;
    private int reservedBikes;
    private int reservedSlots;

    public Station(int id, final GeoPoint position, int capacity, List<Bike> bikes) {
        this.id = id;
        this.position = position;
        this.capacity = capacity;
        this.bikes = bikes;
        this.reservedBikes = 0;
        this.reservedSlots = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public List<Bike> getBikes() {
        return bikes;
    }

    public int getReservedBikes() {
        return reservedBikes;
    }

    public void reservesBike() {
        this.reservedBikes++;
    }

    public void cancelsBikeReservation() {
        this.reservedBikes--;
    }

    public int getReservedSlots() {
        return reservedSlots;
    }

    public void reservesSlot() {
        this.reservedSlots++;
    }

    public void cancelsSlotReservation() {
        this.reservedSlots--;
    }

    public int availableBikes() {
        return (int)bikes.stream().filter(Objects::nonNull).count() - reservedBikes;
    }

    public int availableSlots() {
        return this.capacity - availableBikes() - reservedSlots;
    }

    public Bike removeBike() {
        Bike bike = null;
    	if (this.availableBikes() == 0) {
           return null;
        }
        for (int i = 0; i < bikes.size(); i++) {
            bike = bikes.get(i);
            if (bike != null) {
                bikes.set(i, null);
                break;       
            }
        }

        return bike;
    }

    public boolean returnBike(Bike bike) {
        boolean result = false;
    	if (this.availableSlots() == 0) {
            return false;
        }
        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i) == null) {
                bikes.set(i, bike);
                result = true;
                break;
            }
        }
		return result;
    }

    @Override
    public String toString() {
        String result = "Id: " + getId();
    	result += " | Position " + position.toString();
        result += " | Capacity: " + capacity;
        result += " | Number of bikes: " + availableBikes() + "\n";
        return result;
    }
}
