package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import java.util.ArrayList;
import java.util.List;

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
	
	public Truck(int capacity) {
		this.id = idGenerator.next();
		this.destinationStation = null;
		this.capacity = capacity;
		this.numberOfBikes = 0;
		this.bikes = new ArrayList<>();
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
	
	public void load(int nBikes) {
		List<Bike> stationBikes = destinationStation.getBikes();
		int count, i = 0;
		
		while (count < nBikes && i < stationBikes.size()) {
			bikes.add(stationBikes.remove(i));
			count++;
			i++;
		}
	}
	
	public void unload(int nBikes) {
		for (int i=0; i<nBikes; i++) {
			destinationStation.add(bikes.remove(i));
		}
	}

}
