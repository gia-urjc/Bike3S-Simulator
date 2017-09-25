package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class ConfigInfo {
	

	private ArrayList<Station> stations;
	private ArrayList<EntryPoint> entryPoints;
	private final int timeReserve;
	
	public ConfigInfo() {
		this.stations = new ArrayList<>();
		this.setEntryPoints(new ArrayList<>());
		this.timeReserve = 0; 
	}
	
	public ConfigInfo(ArrayList<Station> stations, ArrayList<EntryPoint> entryPoints, int timeReserve) {
		this.stations = stations;
		this.entryPoints = entryPoints;
		this.timeReserve = timeReserve;
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

	@Override
	public String toString() {
		String result = "====================\n";
		result += "prueba";
		return result;
	}

	
}
