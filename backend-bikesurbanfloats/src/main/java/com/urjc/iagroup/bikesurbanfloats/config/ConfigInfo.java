package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.ArrayList;

import com.urjc.iagroup.bikesurbanfloats.core.Bike;
import com.urjc.iagroup.bikesurbanfloats.core.Station;

public class ConfigInfo {
	
	private ArrayList<Bike> bikes;
	private ArrayList<Station> stations;
	private String distribution;
	
	public ConfigInfo() {
		this.bikes = new ArrayList<>();
		this.stations = new ArrayList<>();
		this.distribution = null;
	}
	
	public ConfigInfo(ArrayList<Bike> bikes, ArrayList<Station> stations, String distribution) {
		super();
		this.bikes = bikes;
		this.stations = stations;
		this.distribution = distribution;
	}
	
	public ArrayList<Bike> getBikes() {
		return bikes;
	}
	
	public void setBikes(ArrayList<Bike> bikes) {
		this.bikes = bikes;
	}
	
	public ArrayList<Station> getStations() {
		return stations;
	}
	
	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}
	
	public String getDistribution() {
		return distribution;
	}
	
	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}
	
	

}
