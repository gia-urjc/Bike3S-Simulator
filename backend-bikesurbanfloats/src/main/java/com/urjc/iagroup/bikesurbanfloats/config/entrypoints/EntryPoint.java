package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.events.Event;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public interface EntryPoint {
	
	public List<Event> generateEvents(IdGenerator personIdGenerator);
	
}
