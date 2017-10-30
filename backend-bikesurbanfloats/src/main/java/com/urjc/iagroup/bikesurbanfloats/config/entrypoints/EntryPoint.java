package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.util.List;

/**
 * It is an event generator for user appearances
 * It represents an entry point at system geographic map where a unique user or several users appear and start interacting with the system 
 * @author IAgroup
 *
 */

public interface EntryPoint {
	
	List<EventUserAppears> generateEvents(SimulationConfiguration simulationConfiguration);

}
