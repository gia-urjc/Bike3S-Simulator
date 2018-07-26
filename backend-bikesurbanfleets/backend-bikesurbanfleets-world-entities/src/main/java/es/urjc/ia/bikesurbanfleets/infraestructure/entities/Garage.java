package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

import java.util.ArrayList;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.util.IdGenerator;

public class Garage implements Entity {
	private static IdGenerator idGenerator = new IdGenerator();
	private int id;
	private int capacity;
	private GeoPoint position;
	private List<Truck> trucks;

	public Garage(int capacity, GeoPoint position) {
		this.id = idGenerator.next();
		this.capacity = capacity;
		this.position = position;
		this.trucks = new ArrayList<>();	
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

	public List<Truck> getTrucks() {
		return trucks;
	}
	
	
	

}
