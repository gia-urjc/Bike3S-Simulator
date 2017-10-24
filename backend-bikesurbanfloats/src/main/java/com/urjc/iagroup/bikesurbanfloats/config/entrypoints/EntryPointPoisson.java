package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class EntryPointPoisson implements EntryPoint {
	
	private GeoPoint position;
	private double radio; //meters
	private DistributionPoisson distribution;
	private UserType userType;
	private TimeRange timeRange;
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, UserType userType) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = null;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.radio = radio;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange,  double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radio = radio;
	}


	private User createUser(UserFactory userFactory) {
		BoundingCircle bcircle = new BoundingCircle(position, radio);
		User user;
		if(radio > 0.0) {
			GeoPoint randomPosition = bcircle.randomPointInCircle();
			user = userFactory.createUser(userType, randomPosition);
		}
		else {
			user = userFactory.createUser(userType, position);
		}
		return user;
	}

	@Override
	public List<EventUserAppears> generateEvents(SimulationConfiguration simulationConfiguration) {
		
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		int actualTime, endTime;
		if(timeRange == null) {
			actualTime = 0;
			endTime = simulationConfiguration.getTotalTimeSimulation();
		}
		else {
			actualTime = timeRange.getStart();
			endTime = timeRange.getEnd();
		}
		while(actualTime < endTime) {
			User user = createUser(userFactory);
			int timeEvent = distribution.randomInterarrivalDelay();
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, user, simulationConfiguration);
			generatedEvents.add(newEvent);
		}
		return generatedEvents;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution.getDistribution();
		result += "| distributionParameter " + distribution.getLambda() + "\n";
		result += "user Type: " + userType;
		return result;
	}
	
}
