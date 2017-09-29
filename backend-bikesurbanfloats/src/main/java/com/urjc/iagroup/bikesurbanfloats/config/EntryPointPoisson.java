package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.LinkedList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factory.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factory.PersonType;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.MathDistributions;

public class EntryPointPoisson implements EntryPoint {
	
	
	private GeoPoint position;
	private DistributionType distribution;
	private double parameterDistribution;
	private PersonType personType;

	public EntryPointPoisson(GeoPoint location, DistributionType distribution, double parameterDistribution, String className, PersonType personType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.position = location;
		this.distribution = distribution;
		this.parameterDistribution = parameterDistribution;
		this.personType = personType;
	}

	@Override
	public List<Event> generateEvents(IdGenerator personIdGenerator) {
		int actualTime = 0;
		List<Event> generatedEvents = new LinkedList<>();
		PersonFactory personFactory = new PersonFactory();
		while(actualTime < SystemInfo.totalTimeSimulation) {
			int id = personIdGenerator.next();
			Person person = personFactory.createPerson(id, personType, position);
			double lambda = parameterDistribution;
			int timeEvent = MathDistributions.poissonRandomInterarrivalDelay(lambda);
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
