package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;

public interface Event extends Comparable<Event> {
	int getInstant();
	List<Event> execute();
	int compareTo(Event event);
	String toString();
  
}