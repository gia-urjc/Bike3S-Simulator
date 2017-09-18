package com.urjc.iagroup.bikesurbanfloats.core;

import java.util.ArrayList;

public class Station {

	private ArrayList<Bike> bikes;
	private GeoPoint location;

	public Station(ArrayList<Bike> bikes) {
		this.bikes = bikes;
	}

	public ArrayList<Bike> getBikes() {
		return bikes;
	}

	public void setBikes(ArrayList<Bike> bikes) {
		this.bikes = bikes;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}
	
	
}
