package com.urjc.iagroup.bikesurbanfloats.config;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class EntryPoint {

	private static final String POISON_DISTRIBUTION = "poisson";
	
	
	private GeoPoint position;
	private String distribution;
	private double parameterDistribution;

	public EntryPoint(GeoPoint location, String distribution, double parameterDistribution) {
		this.position = location;
		this.distribution = distribution;
		this.parameterDistribution = parameterDistribution;
	}

	public GeoPoint getLocation() {
		return position;
	}

	public void setLocation(GeoPoint location) {
		this.position = location;
	}

	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public double getParameterDistribution() {
		return parameterDistribution;
	}

	public void setParameterDistribution(double parameterDistribution) {
		this.parameterDistribution = parameterDistribution;
	}
	
//	public List<Event> generateEvents() {
//		if(this.distribution.equals(POISON_DISTRIBUTION)) {
//			// To do
//		}
//	}
	
}
