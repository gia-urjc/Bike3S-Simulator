package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;

public interface Event extends Comparable<Event> {
	int getInstant();
	List<Event> execute();

	default int compareTo(Event event) {
	    return Integer.compare(this.getInstant(), event.getInstant());
    }
  
}