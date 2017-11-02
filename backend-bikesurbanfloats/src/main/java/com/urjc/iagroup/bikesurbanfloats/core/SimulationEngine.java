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
	
	/**
	 * It creates an event quee where its events are sorted by the time instant when they'll ocur.   
		 */
	public SimulationEngine(SimulationConfiguration simulationConfiguration, SystemManager systemManager) {
		eventsQueue = new PriorityQueue<>(simulationConfiguration.getEventUserAppears());
		this.simulationConfiguration = simulationConfiguration;
		this.systemManager = systemManager;
		simulationConfiguration.getEventUserAppears().stream().map(EventUser::getUser).forEach(user -> user.setSystemManager(systemManager));
	}
	
<<<<<<< HEAD
	/**
	 * It executes, in the corresponding order, all the events are generated through the entire simulation,	i. e., 
	 * it executes all the initial events of the quee, as well as thouse that these executions generate.
	 * Moreover, this method initialize history class and registers all the system entities in that class.   
	 */
	public void run() {
	    History.init(simulationConfiguration);
	    simulationConfiguration.getEventUserAppears().stream().map(EventUser::getUser).forEach(History::registerNewEntity);
	    systemManager.consultStations().forEach(History::registerNewEntity);
	    systemManager.consultBikes().forEach(History::registerNewEntity);
=======
	public void run() throws Exception {

	    History.init(simulationConfiguration);

	    simulationConfiguration.getEventUserAppears().stream().map(EventUser::getUser).forEach(History::registerEntity);
	    systemManager.consultStations().forEach(History::registerEntity);
	    systemManager.consultBikes().forEach(History::registerEntity);
>>>>>>> 1ec91a77605e3c3a3f367f3eeec34cc0a901afa8

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

		History.close();
	}

}