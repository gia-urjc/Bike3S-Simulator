package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.events.*;

import com.urjc.iagroup.bikesurbanfloats.config.*;

import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;


public class SimulationEngine {

    private List<EventUserAppears> userAppearsList = new ArrayList<>();
	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	private SystemInfo systemInfo;
	
	public SimulationEngine(SystemInfo systemInfo) {
		eventsQueue = new PriorityQueue<Event>();
		this.systemInfo = systemInfo;
	}
	
	public void processEntryPoints() {
		List<EventUserAppears> events = systemInfo.getEventUserAppears();
		for(EventUserAppears event: events) {
			userAppearsList.add(event);
			systemInfo.getUsers().add(event.getUser());
		}
        eventsQueue.addAll(userAppearsList);
		
	}
	
	public void run() {
		
		
        //History.init(userAppearsList, systemInfo);

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