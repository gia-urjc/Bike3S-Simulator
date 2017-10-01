package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public interface EntryPoint {
	
	public List<EventUserAppears> generateEvents(IdGenerator personIdGenerator);
	
}
