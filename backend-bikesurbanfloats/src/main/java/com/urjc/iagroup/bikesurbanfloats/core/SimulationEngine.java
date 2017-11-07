package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUser;
import com.urjc.iagroup.bikesurbanfloats.history.History;

import java.util.List;
import java.util.PriorityQueue;


public class SimulationEngine {

	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	private SimulationConfiguration simulationConfiguration;
	private SystemManager systemManager;
	
	public SimulationEngine(SimulationConfiguration simulationConfiguration, SystemManager systemManager) {
		eventsQueue = new PriorityQueue<>(simulationConfiguration.getEventUserAppears());
		this.simulationConfiguration = simulationConfiguration;
		this.systemManager = systemManager;
		simulationConfiguration.getEventUserAppears().stream().map(EventUser::getUser).forEach(user -> user.setSystemManager(systemManager));
	}
	
	public void run() throws Exception {

	    History.init(simulationConfiguration);

		while (!eventsQueue.isEmpty()) {
			Event event = eventsQueue.poll();  // retrieves and removes first element
			List<Event> newEvents = event.execute();
			System.out.println(event.toString());
			eventsQueue.addAll(newEvents);
			History.registerEvent(event);
		}

		History.close();
	}

}