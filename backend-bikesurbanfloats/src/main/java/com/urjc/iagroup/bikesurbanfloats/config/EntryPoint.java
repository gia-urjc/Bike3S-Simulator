package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.LinkedList;
import java.util.List;import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.Distribution;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.MathDistributions;

public class EntryPoint {
	
	private GeoPoint position;
	private Distribution distribution;
	private double distributionParameter;

	public EntryPoint(GeoPoint location, Distribution distribution, double parameterDistribution) {
		this.position = location;
		this.distribution = distribution;
		this.distributionParameter = parameterDistribution;
	}

	public GeoPoint getLocation() {
		return position;
	}

	public void setLocation(GeoPoint location) {
		this.position = location;
	}

	public Distribution getDistribution() {
		return distribution;
	}

	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}

	public double getParameterDistribution() {
		return distributionParameter;
	}

	public void setParameterDistribution(double parameterDistribution) {
		this.distributionParameter = parameterDistribution;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution;
		result += "| distributionParameter" + distributionParameter + "\n";
		return result;
	}
	
	public List<Event> generateEvents(int totalTimeSimulation) {
		double actualTime = 0;
		List<Event> generatedEvents = new LinkedList<>();
		if(this.distribution.equals(Distribution.POISSON)) {
			while(actualTime < totalTimeSimulation) {
				double timeEvent = MathDistributions.poissonRandomInterarrivalDelay(distributionParameter);
				actualTime += timeEvent;
				// TODO: Event newEvent = new EventUserAppears(actualTime, new Person);
				System.out.println(actualTime);
			}
		}
		// TODO
		return null;
	}
	
}
