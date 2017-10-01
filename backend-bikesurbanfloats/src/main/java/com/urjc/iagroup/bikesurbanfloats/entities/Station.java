package com.urjc.iagroup.bikesurbanfloats.entities;

import com.sun.istack.internal.NotNull;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Objects;

public class Station extends Entity {

    private final GeoPoint position;

    private int capacity;
    private List<Bike> bikes;
    private int reservedBikes;
    private int reservedSlots;

    public Station(int id, @NotNull final GeoPoint position, int capacity, List<Bike> bikes) {
        super(id);
        this.position = position;
        this.capacity = capacity;
        this.bikes = bikes;
        this.reservedBikes = 0;
        this.reservedSlots = 0;
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

    public Bike removeBike() throws ServiceUnavailableException {
        if (this.availableBikes() == 0) {
            throw new ServiceUnavailableException("Trying to remove a bike while there are none available!");
        }

        for (int i = 0; i < bikes.size(); i++) {
            Bike bike = bikes.get(i);
            if (bike != null) {
                bikes.add(i, null);
                return bike;
            }
        }

        return null;
    }

    public void returnBike(Bike bike) throws ServiceUnavailableException {
        if (this.availableSlots() == 0) {
            throw new ServiceUnavailableException("Trying to return a bike while there are no free slots!");
        }
        this.bikes.add(bike);

        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i) == null) {
                bikes.add(i, bike);
                break;
            }
        }
    }

    @Override
    public String toString() {
        String result = "| " + position.toString();
        result += "| Capacity: " + capacity;
        result += "| Number of bikes: " + bikes.size() + "\n";
        return result;
    }
}
