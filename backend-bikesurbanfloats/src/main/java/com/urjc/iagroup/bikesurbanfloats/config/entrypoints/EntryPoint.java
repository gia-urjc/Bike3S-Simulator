package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

public interface EntryPoint {
	
	public List<EventUserAppears> generateEvents(SystemInfo systemInfo);
	
}
