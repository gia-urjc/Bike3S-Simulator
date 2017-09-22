package com.urjc.iagroup.bikesurbanfloats.config;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class EntryPoint {
	
	private GeoPoint location;
	private String distribution;
	private double parameterDistribution;

	public EntryPoint(GeoPoint location, String distribution, double parameterDistribution) {
		this.location = location;
		this.distribution = distribution;
		this.parameterDistribution = parameterDistribution;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
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
//			// TODO: Poisson distribution
//		}
//	}
	
}
