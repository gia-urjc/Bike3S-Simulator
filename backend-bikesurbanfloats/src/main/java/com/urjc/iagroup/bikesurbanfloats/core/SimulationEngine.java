package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUser;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.history.History;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This is the most important class which makes the simulation possible.
 * It uses a queue to manage the events proccessing.
 * @author IAgroup
 *
 */
public class SimulationEngine {

	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	private SimulationConfiguration simulationConfiguration;
	private SystemManager systemManager;
	
	/**
	 * It creates an event queue where its events are sorted by the time instant when they'll occur.
	 */
	public SimulationEngine(SimulationConfiguration simulationConfiguration, SystemManager systemManager) {
		this.simulationConfiguration = simulationConfiguration;
		this.systemManager = systemManager;
		this.eventsQueue = new PriorityQueue<>(processEntryPoints());
	}

	private List<EventUserAppears> processEntryPoints() {
	    List<EventUserAppears> eventUserAppearsList = new ArrayList<>();

	    simulationConfiguration.getEntryPoints().stream()
                .map(EntryPoint::generateEvents)
                .flatMap(List::stream)
                .forEach(eventUserAppearsList::add);

	    eventUserAppearsList.stream()
                .map(EventUser::getUser)
                .forEach(user -> user.setSystemManager(systemManager));

	    return eventUserAppearsList;
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