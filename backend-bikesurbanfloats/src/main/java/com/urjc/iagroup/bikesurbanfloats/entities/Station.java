package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.entities.models.StationModel;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricStation;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

import java.util.List;
import java.util.Objects;

/**
 * This is the second main entity of the system
 * It represents station state: how many bikes and slots contains and which of them are reserved
 * It provides all actions a user can carry out with bikes (to remove, return or reserve them) and slots (to reserve)  
 * @author IAgroup
 *
 */
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
    	Bike bike = null;
    	for (Bike currentBike: bikes) {
    		if (currentBike != null &&	!currentBike.isReserved()) {
    			bike = currentBike;
    			break;
    		}
    	}
    	return bike;
    }
    
    /**
     * Station locks a bike for a user if there're available bikes
     * @return bike which has been reserved or bike with null value if there're no available bikes
     */

    public Bike reservesBike() {
    	Bike bike = null;
    	if (availableBikes() > 0) {
	        bike = getFirstAvailableBike();
	        this.reservedBikes++;
	        bike.setReserved(true);
    	}
        return bike;
    }
    
    /**
     * Station unlocks a bike to make it available for other users 
     */

    public void cancelsBikeReservation() {
        this.reservedBikes--;
    }
    
    /**
     * Station locks a slot for a user if there're available slots 
     */
    
    public void reservesSlot() {
    	if (availableSlots() > 0)
        this.reservedSlots++;
    }
    
    /**
     * Station unlocks a slot to make it available for other users
     */

    public void cancelsSlotReservation() {
        this.reservedSlots--;
    }
    
    /**
     * If there's one available bike at station, user can remove it leaving an available slot at station     
     * @return a bike if there's one available or null if there's no available bikes 
     */
    
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
        
        /**
         * If there's available slots at station, it places a bike (which a user has returned) on a slot  
         * @param bike: it is the bike which user wants to return
         * @return true if returning the bike to station has been possible and false in other case (there's no available slots)
         */

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