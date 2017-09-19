package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.core.Station;

public class ConfigInfo {
	

	private ArrayList<Station> stations;
	private ArrayList<EntryPoint> entryPoints;
	
	public ConfigInfo() {
		this.stations = new ArrayList<>();
		this.setEntryPoints(new ArrayList<>());
	}
	
	public ConfigInfo(ArrayList<Station> stations, String distribution) {
		this.stations = stations;
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
		return "Stations=" + stations + ", entryPoints=" + entryPoints + "]";
	}

	
}
