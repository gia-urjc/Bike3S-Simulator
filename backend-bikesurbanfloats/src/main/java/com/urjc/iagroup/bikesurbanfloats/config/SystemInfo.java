package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class SystemInfo {
	
	//System Information
	public ArrayList<Station> stations = new ArrayList<>();
	public ArrayList<EntryPoint> entryPoints = new ArrayList<>();
	public ArrayList<Bike> bikes = new ArrayList<>();
	public ArrayList<Person> persons = new ArrayList<>();

	//Configuration information
	public int reservationTime = 0;
	public int totalTimeSimulation = 0;
	public long randomSeed = 0;
	
	//IdsGenerator
	public IdGenerator bikeIdGen = new IdGenerator();
	public IdGenerator stationIdGen = new IdGenerator();
	public IdGenerator userIdGenerator = new IdGenerator();
	
	//Utils
	public BoundingBox boundingBox = null;
	
	
	SystemInfo() {}
	
	public void resetInfo() {
		stations = new ArrayList<>();
		entryPoints = new ArrayList<>();
		bikes = new ArrayList<>();
		persons = new ArrayList<>();
		reservationTime = 0;
		totalTimeSimulation = 0;
		randomSeed = 0;
		boundingBox = null;
	}
	
	public String toString() {
		String result = "";
		result += "STATIONS: \n ------------- \n";
		for(Station s: stations) {
			result += s.toString() + "\n";
		}
		result += "Entry Points: \n ------------- \n";
		for(EntryPoint e: entryPoints) {
			result += e.toString() + "\n";
		}
		result += "Reservation time: " + reservationTime + "\n";
		result += "Total time simulation: " + totalTimeSimulation + "\n";
		result += "Random seed: " + randomSeed + "\n";
		return result;
	}

}
