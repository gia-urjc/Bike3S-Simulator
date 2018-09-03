package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;

public class Garage implements Entity {
	private static IdGenerator idGenerator = new IdGenerator();
	private int id;
	private int capacity;
	private GeoPoint position;
	private Map<Integer, Truck> trucks;

	public Garage(int capacity, GeoPoint position) {
		this.id = idGenerator.next();
		this.capacity = capacity;
		this.position = position;
		this.trucks = new HashMap<>();	
	}

	public int getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public GeoPoint getPosition() {
		return position;
	}

	public Map<Integer, Truck> getTrucks() {
		return trucks;
	}
	
	public int availableParkingLots() {
		return capacity - trucks.size();
	}
	
	public boolean addTruck(Truck truck) {
		if (availableParkingLots() > 0) {
			trucks.put(truck.getId(), truck);
			return true;
		}
		return false;
	}
	
	public Truck removeTruck() {
		List<Truck> garageTrucks = (List<Truck>) trucks.values();
		Truck selectedTruck = garageTrucks.get(0);
		trucks.remove(selectedTruck.getId());
		return selectedTruck;
	}
	
	public Truck removeTruck(int id) {
		Truck truck = null;
		if (trucks.containsKey(id)) {
			truck = trucks.get(id);
			trucks.remove(id);
		}
		return truck;
	}
	
	
}