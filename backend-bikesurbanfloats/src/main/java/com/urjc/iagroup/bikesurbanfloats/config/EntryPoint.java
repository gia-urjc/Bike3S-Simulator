package com.urjc.iagroup.bikesurbanfloats.config;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.events.Event;

public interface EntryPoint {
	
	public List<Event> generateEvents();
	
}
