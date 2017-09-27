package com.urjc.iagroup.bikesurbanfloats.config;

<<<<<<< Updated upstream
=======
import java.util.LinkedList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.personfactory.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.personfactory.PersonType;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.Distribution;
>>>>>>> Stashed changes
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class EntryPoint {

	private static final String POISON_DISTRIBUTION = "poisson";
	
	
	private GeoPoint position;
<<<<<<< Updated upstream
	private String distribution;
	private double parameterDistribution;

	public EntryPoint(GeoPoint location, String distribution, double parameterDistribution) {
		this.position = location;
		this.distribution = distribution;
		this.parameterDistribution = parameterDistribution;
=======
	private Distribution distribution;
	private double distributionParameter;
	private PersonType personType;

	public EntryPoint(GeoPoint location, Distribution distribution, double parameterDistribution, String className, PersonType personType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.position = location;
		this.distribution = distribution;
		this.distributionParameter = parameterDistribution;
		this.personType = personType;
>>>>>>> Stashed changes
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
	
<<<<<<< Updated upstream
//	public List<Event> generateEvents() {
//		if(this.distribution.equals(POISON_DISTRIBUTION)) {
//			// To do
//		}
//	}
=======
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution;
		result += "| distributionParameter" + distributionParameter + "\n";
		return result;
	}
	
	public List<Event> generateEvents(int totalTimeSimulation) {
		int actualTime = 0;
		List<Event> generatedEvents = new LinkedList<>();
		PersonFactory personFactory = new PersonFactory();
		if(this.distribution.equals(Distribution.POISSON)) {
			while(actualTime < totalTimeSimulation) {
				Person person = personFactory.createPerson(personType, position);
				double u = (double) 1 / distributionParameter;
				int timeEvent = MathDistributions.poissonRandomInterarrivalDelay(u);
				actualTime += timeEvent;
				Event newEvent = new EventUserAppears(actualTime, person);
				generatedEvents.add(newEvent);
				System.out.println(actualTime);
			}
		}
		// TODO
		return generatedEvents;
	}
>>>>>>> Stashed changes
	
}
