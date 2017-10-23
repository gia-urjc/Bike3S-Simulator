package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.events.*;

import com.urjc.iagroup.bikesurbanfloats.config.*;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;


public class SimulationEngine {

    private List<EventUserAppears> userAppearsList = new ArrayList<>();
	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	private SystemConfiguration systemConfig;
	
	public SimulationEngine(SystemConfiguration systemConfig) {
		eventsQueue = new PriorityQueue<Event>();
		this.systemConfig = systemConfig;
	}
	
	public void processEntryPoints() {
		List<EventUserAppears> events = systemConfig.getEventUserAppears();
		for(EventUserAppears event: events) {
			userAppearsList.add(event);
			systemConfig.getUsers().add(event.getUser());
		}
        eventsQueue.addAll(userAppearsList);
		
	}
	
	public void run() {
		
		
        //History.init(userAppearsList, systemConfig);

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