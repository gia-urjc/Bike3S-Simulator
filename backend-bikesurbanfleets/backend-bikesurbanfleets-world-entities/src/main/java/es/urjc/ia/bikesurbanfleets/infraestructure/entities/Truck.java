package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import java.util.ArrayList;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;

/**
 * This class represents the system trucks which transport bikes from one station to other/others 
 * in order to balance the system resources.
 * @author IAGroup
 *
 */
public class Truck implements Entity {
	private static IdGenerator idGenerator = new IdGenerator();
	private int id;
	private int capacity;
 	private Station destinationStation;
		private List<Bike> bikes;
	private double velocity;
	private GeoPoint position;
	private GeoRoute route;
	private Order order;
		
	public Truck(int capacity, double velocity) {
		this.id = idGenerator.next();
		this.destinationStation = null;
		this.capacity = capacity;
		this.bikes = new ArrayList<>();
		this.velocity = velocity;
		this.order = null;
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public List<Bike> getBikes() {
		return this.bikes;
	}
	
	public Station getDestinationStation() {
		return this.destinationStation;
	}
	
	public double getVelocity() {
		return velocity;
	}
		
	public GeoPoint getPosition() {
		return position;
	}

	public GeoRoute getRoute() {
		return route;
	}
	
	public Order getOrder() {
		
		return order;
	}

	public void setPosition(GeoPoint position) {
		this.position = position;
	}

	public void setRoute(GeoRoute route) {
		this.route = route;
	}

	public void setDestinationStation(Station destinationStation) {
		this.destinationStation = destinationStation;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public int loadedBikes() {
		return bikes.size();
	}
	
	public int availableSlots() {
		return capacity - loadedBikes();
	}

	public boolean load(int nBikes) {
		if (availableSlots() >= nBikes && destinationStation.availableBikes() >= nBikes) {
			for (int i=0; i<nBikes; i++) {
				Bike bike = destinationStation.removeBikeWithoutReservation();
				bikes.add(bike);
			}
			return true;
		}
		return false;
	}
	
	public boolean unload(int nBikes) {
		if (loadedBikes() >= nBikes && destinationStation.availableSlots() >= nBikes) {
			for (int i=0; i<nBikes; i++) {
				Bike bike = bikes.remove(i); 
				destinationStation.returnBike(bike);
			}
			return true;
		}
		return false;
	}

} 