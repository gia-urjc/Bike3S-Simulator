package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.history.History;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.config.*;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;


public class SimulationEngine {

    private List<EventUserAppears> userAppearsList = new ArrayList<>();
	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	
	public SimulationEngine() {
		eventsQueue = new PriorityQueue<Event>();
	}
	
	public void processConfig() {
		IdGenerator personIdGen = new IdGenerator();
		List<EntryPoint> entryPoints = SystemInfo.entryPoints;
		for(EntryPoint entryPoint: entryPoints) {
			List<EventUserAppears> events = entryPoint.generateEvents(personIdGen);
			for(EventUserAppears event: events) {
				userAppearsList.add(event);
				System.out.println("Added person at instant " + event.getInstant());
			}
		}

        eventsQueue.addAll(userAppearsList);
		
	}
	
	public void run() {

        History.init(userAppearsList);

		while (!eventsQueue.isEmpty()) {
			Event event = eventsQueue.poll();  // retrieves and removes first element
			List<Event> newEvents = event.execute();
			System.out.println(event.toString());

			if (!newEvents.isEmpty()) {
				for(Event newEvent: newEvents) {
					eventsQueue.add(newEvent);
				}
			}
		}
	}

}