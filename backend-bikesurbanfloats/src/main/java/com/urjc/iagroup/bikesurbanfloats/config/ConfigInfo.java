package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class ConfigInfo {
	

	private ArrayList<Station> stations;
	private ArrayList<EntryPoint> entryPoints;
	private final int timeReserve;
	private final int totalTimeSimulation;
	
	public ConfigInfo() {
		this.stations = new ArrayList<>();
		this.setEntryPoints(new ArrayList<>());
		this.timeReserve = 0; 
		this.totalTimeSimulation = 0;
	}
	
	public ConfigInfo(ArrayList<Station> stations, ArrayList<EntryPoint> entryPoints, int timeReserve, int totalTimeSimulation) {
		this.stations = stations;
		this.entryPoints = entryPoints;
		this.timeReserve = timeReserve;
		this.totalTimeSimulation = totalTimeSimulation;
	}
	
	public ArrayList<Station> getStations() {
		return stations;
	}
	
	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}
	
	public ArrayList<EntryPoint> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(ArrayList<EntryPoint> entryPoints) {
		this.entryPoints = entryPoints;
	}
	
	public int getTotalTimeSimulation() {
		return totalTimeSimulation;
	}

	@Override
	public String toString() {
		String result = "==============\n";
		result += "Configuration\n";
		result += "==============\n";
		result += "--------------\n";
		result += "Stations\n";
		result += "--------------\n";
		int index = 1;
		for(Station s: stations) {
			result += "Station " + index + "\n";
			result += "\n";
			result += s.toString();
			result += "\n";
			index++;
		}
		result += "--------------\n";
		result += "Entry Points\n";
		result += "--------------\n";
		
		index = 1;
		for(EntryPoint e: entryPoints) {
			result += "EntryPoint " + index + "\n";
			result += "\n";
			result += e.toString();
			result += "\n";
			index++;
		}
		result += "--------------\n";
		result += "Reservation times\n";
		result += "--------------\n";
		result += "Time Reserve: " + timeReserve + "\n"; 
		return result;
	}

	
}
