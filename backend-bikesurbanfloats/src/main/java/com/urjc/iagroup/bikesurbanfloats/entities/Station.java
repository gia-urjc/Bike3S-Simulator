package com.urjc.iagroup.bikesurbanfloats.entities;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.History;
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
public class Station implements Entity {

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
        History.registerEntity(this);
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

    public int getReservedSlots() {
        return reservedSlots;
    }

    public int availableBikes() {
        return (int)bikes.stream().filter(Objects::nonNull).count() - reservedBikes;
    }

    public int availableSlots() {
        return this.capacity - (int)bikes.stream().filter(Objects::nonNull).count() - reservedSlots;
    }
    
    private Bike getFirstAvailableBike() {
        Bike bike = null;
        for (Bike currentBike: bikes) {
            if (currentBike != null &&    !currentBike.isReserved()) {
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
        bike.setReserved(true);
        this.reservedBikes++;
        }
        return bike;
    }
    
    /**
     * Station unlocks a bike to make it available for other users 
     */

    public void cancelsBikeReservation(Reservation reservation) {
        this.reservedBikes--;
        reservation.getBike().setReserved(false);
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
     * @return a bike if there's one available or null in other case 
     */
    
        public Bike removeBikeWithoutReservation() {
        Bike bike = null;
        if (this.availableBikes() == 0) {
           return null;
        }
        for (int i = 0; i < bikes.size(); i++) {
            bike = bikes.get(i);
            if (bike != null && !bike.isReserved()) {
                bikes.set(i, null);
                break;       
            }
        }
        return bike;
    }
        
        /**
         * Station let the user remove his reserved bike 
         * @param reservation: it is the bike reservation which user has made previously
         * @return the bike user has reserved
         */
        
        public Bike removeBikeWithReservation(Reservation reservation) {
            Bike bike = reservation.getBike();
            int i = bikes.indexOf(bike);
            bikes.set(i, null);
            bike.setReserved(false);
            return bike;
        }
        
        /**
         * If there's available slots at station, it places a bike (which a user has returned) on a slot  
         * @param bike: it is the bike which user wants to return
         * @return true if returning the bike to station has been possible and false in other case (there's no available slots)
         */

    public boolean returnBike(Bike bike) {
        boolean returned = false;
        if (this.availableSlots() == 0) {
            return false;
        }
        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i) == null) {
                bikes.set(i, bike);
                returned = true;
                break;
            }
        }
        return returned;
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