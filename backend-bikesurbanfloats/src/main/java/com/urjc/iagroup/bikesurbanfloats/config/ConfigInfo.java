package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class ConfigInfo {
	
	public static ArrayList<Station> stations;
	public static ArrayList<EntryPoint> entryPoints;
	public static int reservationTime = 0;
	public static int totalTimeSimulation = 0;

<<<<<<< Updated upstream
	private ArrayList<Station> stations;
	private ArrayList<EntryPoint> entryPoints;
	
	public ConfigInfo() {
		this.stations = new ArrayList<>();
		this.setEntryPoints(new ArrayList<>());
	}
	
	public ConfigInfo(ArrayList<Station> stations, ArrayList<EntryPoint> entryPoints) {
		this.stations = stations;
		this.entryPoints = entryPoints;
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

	
=======
>>>>>>> Stashed changes
}
