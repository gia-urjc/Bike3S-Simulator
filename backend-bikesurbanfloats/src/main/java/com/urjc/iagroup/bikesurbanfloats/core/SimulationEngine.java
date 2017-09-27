package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.entities.*;
import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.config.*;
import java.util.PriorityQueue;
import java.util.List;


public class SimulationEngine {
	PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	
	public SimulationEngine() {
		eventsQueue = new PriorityQueue<Event>();
			}
	
	public void processConfig(ConfigInfo config) {
		List<EntryPoint> entryPoints = config.getEntryPoints();
		for(EntryPoint entryPoint: entryPoints) {
			//List<Event> events = entryPoint.generateEvents();
			//for(Event event: events)
			//	eventsQueue.add(event);
		}
		
	}
	
	public void run() {
		for(Event event: eventsQueue)
			event.execute();
	}

}
