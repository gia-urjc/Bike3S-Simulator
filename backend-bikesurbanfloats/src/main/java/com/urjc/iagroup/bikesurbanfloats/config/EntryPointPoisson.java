package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.LinkedList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factory.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factory.PersonType;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.Distribution;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.MathDistributions;

public class EntryPointPoisson implements EntryPoint {
	
	
	private GeoPoint position;
	private Distribution distribution;
	private double parameterDistribution;
	private PersonType personType;

	public EntryPointPoisson(GeoPoint location, Distribution distribution, double parameterDistribution, String className, PersonType personType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.position = location;
		this.distribution = distribution;
		this.parameterDistribution = parameterDistribution;
		this.personType = personType;
	}

	@Override
	public List<Event> generateEvents() {
		int actualTime = 0;
		List<Event> generatedEvents = new LinkedList<>();
		PersonFactory personFactory = new PersonFactory();
		while(actualTime < ConfigInfo.totalTimeSimulation) {
			Person person = personFactory.createPerson(personType, position);
			double u = (double) 1 / parameterDistribution;
			int timeEvent = MathDistributions.poissonRandomInterarrivalDelay(u);
			System.out.println(timeEvent);
			actualTime += timeEvent;
			Event newEvent = new EventUserAppears(actualTime, person);
			generatedEvents.add(newEvent);
		}
		return generatedEvents;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution;
		result += "| distributionParameter" + parameterDistribution + "\n";
		return result;
	}
	
}
