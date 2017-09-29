package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.config.*;
import java.util.PriorityQueue;
import java.util.List;


public class SimulationEngine {
	
	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	
	public SimulationEngine() {
		eventsQueue = new PriorityQueue<Event>();
	}
	
	public void processConfig() {
		IdGenerator personIdGen = new IdGenerator();
		List<EntryPoint> entryPoints = SystemInfo.entryPoints;
		for(EntryPoint entryPoint: entryPoints) {
			List<Event> events = entryPoint.generateEvents(personIdGen);
			for(Event event: events) {
				eventsQueue.add(event);
				System.out.println("Added person at instant " + event.getInstant());
			}
		}
		
	}
	
	public void run() {
		for(Event event: eventsQueue) {
			List<Event> newEvents = event.execute();

			if (!newEvents.isEmpty()) {
				for(Event newEvent: newEvents) {
					eventsQueue.add(newEvent);
				}
			}
		}
	}

}
