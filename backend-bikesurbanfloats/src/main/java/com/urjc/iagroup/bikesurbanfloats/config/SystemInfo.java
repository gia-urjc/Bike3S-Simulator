package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingBox;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class SystemInfo {
	
	private String configStationPath = "configuration/config_stations.json";
	private String configEntryPath = "configuration/config_entry_points.json";
	private String configSimulationPath = "configuration/config_simulation.json";
	
	//System Information
	private List<Station> stations = new ArrayList<>();
	private List<EventUserAppears> eventUserAppears = new ArrayList<>();
	private List<Bike> bikes = new ArrayList<>();
	private List<User> users = new ArrayList<>();


	//Configuration information
	private int reservationTime = 0;
	private int totalTimeSimulation = 0;
	private long randomSeed = 0;
	
	//IdsGenerator
	private IdGenerator bikeIdGen = new IdGenerator();
	private IdGenerator stationIdGen = new IdGenerator();
	private IdGenerator userIdGenerator = new IdGenerator();
	
	//Utils
	private BoundingBox boundingBox = null;

	SystemInfo() {}
	
	public List<Station> getStations() {
		return stations;
	}

	public void setStations(List<Station> stations) {
		this.stations = stations;
	}

	public List<Bike> getBikes() {
		return bikes;
	}

	public void setBikes(ArrayList<Bike> bikes) {
		this.bikes = bikes;
	}
	
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public int getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(int reservationTime) {
		this.reservationTime = reservationTime;
	}

	public int getTotalTimeSimulation() {
		return totalTimeSimulation;
	}

	public void setTotalTimeSimulation(int totalTimeSimulation) {
		this.totalTimeSimulation = totalTimeSimulation;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public IdGenerator getBikeIdGen() {
		return bikeIdGen;
	}

	public void setBikeIdGen(IdGenerator bikeIdGen) {
		this.bikeIdGen = bikeIdGen;
	}

	public IdGenerator getStationIdGen() {
		return stationIdGen;
	}

	public void setStationIdGen(IdGenerator stationIdGen) {
		this.stationIdGen = stationIdGen;
	}

	public IdGenerator getUserIdGenerator() {
		return userIdGenerator;
	}

	public void setUserIdGenerator(IdGenerator userIdGenerator) {
		this.userIdGenerator = userIdGenerator;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}
	
	public List<EventUserAppears> getEventUserAppears() {
		return eventUserAppears;
	}

	public void setEventUserAppears(List<EventUserAppears> eventUserAppears) {
		this.eventUserAppears = eventUserAppears;
	}
	
	public String getConfigStationPath() {
		return configStationPath;
	}

	public void setConfigStationPath(String configStationPath) {
		this.configStationPath = configStationPath;
	}

	public String getConfigEntryPath() {
		return configEntryPath;
	}

	public void setConfigEntryPath(String configEntryPath) {
		this.configEntryPath = configEntryPath;
	}

	public String getConfigSimulationPath() {
		return configSimulationPath;
	}

	public void setConfigSimulationPath(String configSimulationPath) {
		this.configSimulationPath = configSimulationPath;
	}

	
	public void resetInfo() {
		stations = new ArrayList<>();
		eventUserAppears = new ArrayList<>();
		bikes = new ArrayList<>();
		setUsers(new ArrayList<>());
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
		for(EventUserAppears e: eventUserAppears) {
			result += e.toString() + "\n";
		}
		result += "Reservation time: " + reservationTime + "\n";
		result += "Total time simulation: " + totalTimeSimulation + "\n";
		result += "Random seed: " + randomSeed + "\n";
		return result;
	}

}
