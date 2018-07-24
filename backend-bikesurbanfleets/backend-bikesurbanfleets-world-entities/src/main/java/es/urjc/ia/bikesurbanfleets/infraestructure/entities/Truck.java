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
	private int numberOfBikes;
	private List<Bike> bikes;
	private double velocity;
	private GeoPoint position;
	private GeoRoute route;
		
	public Truck(int capacity, double velocity) {
		this.id = idGenerator.next();
		this.destinationStation = null;
		this.capacity = capacity;
		this.numberOfBikes = 0;
		this.bikes = new ArrayList<>();
		this.velocity = velocity;
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
	
	public int getNumberofBikes() {
		return this.numberOfBikes;
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

	public void setPosition(GeoPoint position) {
		this.position = position;
	}

	public GeoRoute getRoute() {
		return route;
	}

	public void setRoute(GeoRoute route) {
		this.route = route;
	}

	public void setDestinationStation(Station destinationStation) {
		this.destinationStation = destinationStation;
	}

	public void load(int nBikes) {
		if (destinationStation.availableBikes() >= nBikes) {
			for (int i=0; i<nBikes; i++) {
				Bike bike = destinationStation.removeBikeWithoutReservation();
				bikes.add(bike);
			}
			numberOfBikes += nBikes;
		}
		// TODO: add else branch to consult system operator
	}
	
	public void unload(int nBikes) {
		if (destinationStation.availableSlots() >= nBikes) {
			for (int i=0; i<nBikes; i++) {
				Bike bike = bikes.remove(i); 
				destinationStation.returnBike(bike);
			}
			numberOfBikes -= nBikes;
		}
		// TODO: add else branch to consult system operator
	}

} 