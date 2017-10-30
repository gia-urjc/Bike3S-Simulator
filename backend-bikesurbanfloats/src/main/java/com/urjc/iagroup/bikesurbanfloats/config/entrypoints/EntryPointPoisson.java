package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.User.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;

import java.util.ArrayList;
import java.util.List;

public class EntryPointPoisson extends EntryPoint {
	
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

	@Override
	public List<EventUserAppears> generateEvents() {
		
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		int actualTime, endTime;
		if(timeRange == null) {
			actualTime = 0;
			endTime = TOTAL_TIME_SIMULATION;
		}
		else {
			actualTime = timeRange.getStart();
			endTime = timeRange.getEnd();
		}
		while(actualTime < endTime) {
			User user = userFactory.createUser(userType);
			GeoPoint userPosition;
			if(radio > 0) {
				BoundingCircle boundingCircle = new BoundingCircle(position, radio);
				userPosition = boundingCircle.randomPointInCircle();
			}
			else {
				userPosition = position;
			}
			int timeEvent = distribution.randomInterarrivalDelay();
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, user, userPosition);
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
