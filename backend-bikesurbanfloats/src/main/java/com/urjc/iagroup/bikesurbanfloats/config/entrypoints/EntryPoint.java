package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import java.util.List;

public abstract class EntryPoint {
	
	public static int TOTAL_TIME_SIMULATION;
	
	public abstract List<EventUserAppears> generateEvents();
	
}
