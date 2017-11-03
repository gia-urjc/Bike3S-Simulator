package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents several users that appears at system with a Poisson distribution, taking as a reference to generate them the same entry point
 * It provides a method which generates a variable set of users
 * Number of users that are generated depends on the value of parameter of followed distribution  
 * @author IAgroup
 *
 */

public class EntryPointPoisson extends EntryPoint {
	
	/**
 * If a radius is given, position is the center of circle
 * In other case, position is the specific point where user appears
	 */
	private GeoPoint position;
	/**
	 * It is the radius of circle is going to be used to delimit area where users appears  
	 */
	private double radius;
	/**
	 * Type of distribution that users generation will follow
	 */
	private DistributionPoisson distribution;
	/**
	 * Type of users that will be generated  
	 */
	private UserType userType;
	/**
	 * It is the range of time within which users can appears, i. e., 
	 */
	private TimeRange timeRange;  
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, UserType userType) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = null;
		this.radius = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.radius = radio;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radius = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange,  double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radius = radio;
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
			if(radius > 0) {
				BoundingCircle boundingCircle = new BoundingCircle(position, radius);
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
