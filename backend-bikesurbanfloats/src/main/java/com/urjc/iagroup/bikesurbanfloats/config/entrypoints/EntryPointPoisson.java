package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.PersonType;

public class EntryPointPoisson implements EntryPoint {
	
	private GeoPoint position;
	private DistributionPoisson distribution;
	private PersonType personType;

	public EntryPointPoisson(GeoPoint location, DistributionPoisson distribution, double parameterDistribution, String className, PersonType personType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.position = location;
		this.distribution = distribution;
		this.personType = personType;
	}


	@Override
	public List<Event> generateEvents(IdGenerator personIdGenerator) {
		int actualTime = 0;
		List<Event> generatedEvents = new ArrayList<>();
		PersonFactory personFactory = new PersonFactory();
		while(actualTime < SystemInfo.totalTimeSimulation) {
			int id = personIdGenerator.next();
			Person person = personFactory.createPerson(id, personType, position);
			int timeEvent = distribution.randomInterarrivalDelay();
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, person);
			generatedEvents.add(newEvent);
		}
		return generatedEvents;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution.getDistribution();
		result += "| distributionParameter " + distribution.getLambda() + "\n";
		return result;
	}
	
}
