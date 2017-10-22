package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserType;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

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

	private User createUser(IdGenerator userIdGenerator, UserFactory userFactory, SystemInfo systemInfo) {
		int id = userIdGenerator.next();
		BoundingCircle bcircle = new BoundingCircle(position, radio, systemInfo.random);
		User user;
		if(radio > 0.0) {
			GeoPoint randomPosition = bcircle.randomPointInCircle();
			user = userFactory.createUser(id, userType, randomPosition, systemInfo);
		}
		else {
			user = userFactory.createUser(id, userType, position, systemInfo);
		}
		return user;
	}

	@Override
	public List<EventUserAppears> generateEvents(SystemInfo systemInfo) {
		
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		int actualTime, endTime;
		IdGenerator userIdGenerator = systemInfo.userIdGenerator;
		RandomUtil random = systemInfo.random;
		if(timeRange != null) {
			actualTime = 0;
			endTime = systemInfo.totalTimeSimulation;
		}
		else {
			actualTime = timeRange.getStart();
			endTime = timeRange.getEnd();
		}
		while(actualTime < endTime) {
			User user = createUser(userIdGenerator, userFactory, systemInfo);
			int timeEvent = distribution.randomInterarrivalDelay(random);
			System.out.println(timeEvent);
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, user, systemInfo);
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
