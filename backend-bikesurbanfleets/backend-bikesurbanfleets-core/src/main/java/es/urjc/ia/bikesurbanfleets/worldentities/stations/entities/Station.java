package es.urjc.ia.bikesurbanfleets.worldentities.stations.entities;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.deserializers.StationDeserializer;
import es.urjc.ia.bikesurbanfleets.worldentities.users.User;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This is the second main entity of the system It represents station state: how
 * many bikes and slots contains and which of them are reserved It provides all
 * actions a user can carry out with bikes (to remove, return or reserve them)
 * and slots (to reserve)
 *
 * @author IAgroup
 *
 */
@JsonAdapter(StationDeserializer.class)
public class Station implements Entity {

    
    private int id;
    private final GeoPoint position;
    private int capacity;
    private List<Bike> slots;  //the can have or not a bike placed on it
    private TreeMap<Integer, Reservation> reservations;

    private int reservedBikes;
    private int reservedSlots;

    public static Map<Integer,Boolean> idMap;

    public static void resetIdMap() {
        idMap = new HashMap<Integer,Boolean>();
    }

    public Station(GeoPoint position, int capacity, List<Bike> slots, int oficialID) {
        if (idMap.containsKey(oficialID)){
            throw new RuntimeException("duplicate StationIds");
        }
        this.id = oficialID;
        idMap.put(id, Boolean.TRUE);
        this.position = position;
        this.capacity = capacity;
        this.slots = slots;
        this.reservedBikes = 0;
        this.reservedSlots = 0;
        this.reservations = new TreeMap<Integer, Reservation>();
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

    public List<Bike> getSlots() {
        return slots;
    }

    public TreeMap<Integer, Reservation> getReservations(){
        return reservations;
    }
            
    public int getReservedBikes() {
        return reservedBikes;
    }

    public int getReservedSlots() {
        return reservedSlots;
    }

    public int availableBikes() {
        return (int) slots.stream().filter(Objects::nonNull).count() - reservedBikes;
    }

    public int availableSlots() {
        return this.capacity - (int) slots.stream().filter(Objects::nonNull).count() - reservedSlots;
    }

    private int getIndexOfFirstAvailableBike() {
        int index = -1;
        for (int i = 0; i < slots.size(); i++) {
        	if (slots.get(i) != null && !slots.get(i).isReserved()) {
        		index = i;  
        		break;
        	}
        }
        return index;
    }
    
    private int getIndexOfBike(int id) {
    	int index = -1;
    	for (int i = 0; i < slots.size(); i++) {
    		if (slots.get(i) != null && slots.get(i).getId() == id) {
    			index = i;
    			break;
    		}
    	}
    	return index;
    }

    /**
     * Station locks a bike for a user if there're available bikes
     *
     * @return bike which has been reserved or bike with null value if there're
     * no available bikes
     */
    public Reservation getBikeReservation(User user, int instant) {
        Bike bike = null;
        Reservation r = null;
        if (availableBikes() > 0) {
            int i = getIndexOfFirstAvailableBike();
            if (i < 0 || i >= slots.size()) throw new RuntimeException("Index of available bike found out of bound"); 
            bike = slots.get(i);
            bike.setReserved(true);
            this.reservedBikes++;
            r = new Reservation(instant, user, Reservation.ReservationType.BIKE, this, bike, true);
            reservations.put(r.getId(), r);
        }
        else {
        	r = new Reservation(instant, user, Reservation.ReservationType.BIKE, this, null, false);
        }
        user.addReservation(r);
        return r;
    }

    /**
     * Station unlocks a bike to make it available for other users
     */
    public void cancelBikeReservationByTimeout(Reservation reservation, int instant) {
        Reservation res = reservations.get(reservation.getId());
        if (!res.equals(reservation) || res.getState() != Reservation.ReservationState.ACTIVE) {
            throw new RuntimeException("invalid program state: cancelsBikeReservation");
        }
        reservation.expire(instant);
        Bike bike = reservation.getBike();
        bike.setReserved(false);
        this.reservedBikes--;
        User user = reservation.getUser();
        user.cancelReservationByTimeout(reservation);
        reservations.remove(reservation.getId());
    }

    public Reservation getSlotReservation(User user, int instant) {
    	Reservation r = null;
        if (availableSlots() > 0) {
            this.reservedSlots++;
            r = new Reservation(instant, user, Reservation.ReservationType.SLOT, this, true);
            reservations.put(r.getId(), r);
        }
        else {
        	r = new Reservation(instant, user, Reservation.ReservationType.SLOT, this, false);
        }
        user.addReservation(r);
        return r;
    }

    /**
     * Station unlocks a slot to make it available for other users
     */
    public void cancelSlotReservationByTimeout(Reservation reservation, int instant) {
       Reservation res = reservations.get(reservation.getId());
        if (!res.equals(reservation) || res.getState() != Reservation.ReservationState.ACTIVE) {
            throw new RuntimeException("invalid program state: cancelsBikeReservation");
        }
        reservation.expire(instant);
        this.reservedSlots--;
        User user = reservation.getUser();
        user.cancelReservationByTimeout(reservation);
        reservations.remove(reservation.getId());
    }

    /**
     * If there's one available bike at station, user can remove it leaving an
     * available slot at station
     *
     * @return a bike if there's one available or null in other case
     */
    public Bike removeBikeWithoutReservation() {
        Bike bike = null;
        if (this.availableBikes() == 0) {
            return null;
        }
        for (int i = 0; i < slots.size(); i++) {
            bike = slots.get(i);
            if (bike != null && !bike.isReserved()) {
                slots.set(i, null);
                break;
            }
        }
        return bike;
    }

    /**
     * Station let the user remove his reserved bike
     *
     * @param reservation: it is the bike reservation which user has made
     * previously
     * @return the bike user has reserved
     */
    public Bike removeBikeWithReservation(Reservation reservation, User user, int instant) {
        Reservation res = reservations.get(reservation.getId());
        if (!res.equals(reservation) || res.getState() != Reservation.ReservationState.ACTIVE || res.getType() != Reservation.ReservationType.BIKE || res.getUser().getId() != user.getId()
        		|| reservedBikes <= 0) {
            throw new RuntimeException("invalid program state: removeBikeWithReservation");
        }
        Bike bike = reservation.getBike();
        int i = getIndexOfBike(bike.getId());
        if (i == -1) throw new RuntimeException("Reserved bike "+bike.getId()+"  not found");
        slots.set(i, null);
        bike.setReserved(false);
        this.reservedBikes--;
        reservation.resolve(instant);
        reservations.remove(reservation.getId());
        return bike;
    }

    /**
     * Station let the user remove his reserved bike
     *
     * @param reservation: it is the bike reservation which user has made
     * previously
     * @return the bike user has reserved
     */
    public boolean returnBikeWithReservation(Bike bike, Reservation reservation, User user, int instant) {
        Reservation res = reservations.get(reservation.getId());
        if (!res.equals(reservation) || res.getState() != Reservation.ReservationState.ACTIVE
                || res.getType() != Reservation.ReservationType.SLOT || res.getUser().getId() != user.getId()
                || reservedSlots <= 0) {
            throw new RuntimeException("invalid program state: returnBikeWithReservation");
        }
        boolean returned = false;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) == null) {
                slots.set(i, bike);
                returned = true;
                break;
            }
        }
        if (!returned) {
            throw new RuntimeException("invalid program state: returnBikeWithReservation");
        }
        bike.setReserved(false);
        this.reservedSlots--;
        reservation.resolve(instant);
        reservations.remove(reservation.getId());
        return true;
    }

    /**
     * If there's available slots at station, it places a bike (which a user has
     * returned) on a slot
     *
     * @param bike: it is the bike which user wants to return
     * @return true if returning the bike to station has been possible and false
     * in other case (there's no available slots)
     */
    public boolean returnBikeWithoutReservation(Bike bike) {
        boolean returned = false;
        if (this.availableSlots() == 0) {
            return false;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) == null) {
                slots.set(i, bike);
                returned = true;
                break;
            }
        }
        return returned;
    }

    @Override
    public String toString() {
        String result = this.getClass().getSimpleName()+" : | Id (is official ID): " + getId();
        result += " | Position " + position.toString();
        result += " | Capacity: " + capacity;
        result += " | Number of available bikes: " + availableBikes();
        result += " | Number of available slots: " + availableSlots() ;
        result += " | Number of reserved bikes: " + getReservedBikes() ;
        result += " | Number of reserved slots: " + getReservedSlots() ;
        result += " | Number of reservvations: " + reservations.size();
        return result;
    }
 
}
