package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.util.List;

public interface EntryPoint {
	
	List<EventUserAppears> generateEvents(SimulationConfiguration simulationConfiguration);
	
}
