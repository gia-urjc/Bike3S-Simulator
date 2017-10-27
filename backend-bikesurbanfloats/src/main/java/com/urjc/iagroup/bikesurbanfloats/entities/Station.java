package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.models.StationModel;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricStation;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

import java.util.List;
import java.util.Objects;

@HistoryReference(HistoricStation.class)
public class Station implements Entity, StationModel<Bike> {

    private static IdGenerator idGenerator = new IdGenerator();

    private int id;
    private final GeoPoint position;
    private int capacity;
    private List<Bike> bikes;

    private int reservedBikes;
    private int reservedSlots;

    public Station(GeoPoint position, int capacity, List<Bike> bikes) {
        this.id = idGenerator.next();
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
    
    @Override
    public GeoPoint getPosition() {
        return position;
    }
    
    @Override
    public int getCapacity() {
        return this.capacity;
    }
    
    @Override
    public List<Bike> getBikes() {
        return bikes;
    }
    
    @Override
    public int getReservedBikes() {
        return reservedBikes;
    }
    
    @Override
    public int getReservedSlots() {
        return reservedSlots;
    }
    
    @Override
    public int availableBikes() {
        return (int)bikes.stream().filter(Objects::nonNull).count() - reservedBikes;
    }
    
    @Override
    public int availableSlots() {
        return this.capacity - (int)bikes.stream().filter(Objects::nonNull).count() - reservedSlots;
    }
    
    private Bike getFirstAvailableBike() {
    	int i = 0;
    	Bike bike = null;
    	while (bike == null && i < (bikes.size() -1) ) {
    		bike = bikes.get(i);
    	}
    	return bike;
    }

    public Bike reservesBike() {
        this.reservedBikes++;
        Bike bike = getFirstAvailableBike();
        bike.setReserved(true);
        return bike;
    }

    public void cancelsBikeReservation() {
        this.reservedBikes--;
    }
    
    public void reservesSlot() {
        this.reservedSlots++;
    }

    public void cancelsSlotReservation() {
        this.reservedSlots--;
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
        result += " | Number of available bikes: " + availableBikes() + "\n";
        result += " | Number of available slots: " + availableSlots() + "\n";
        return result;
    }
}