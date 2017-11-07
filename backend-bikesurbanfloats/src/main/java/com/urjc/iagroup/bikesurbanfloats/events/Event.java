package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.util.List;

public interface Event extends Comparable<Event> {
	int getInstant();
	List<Event> execute() throws Exception;
	List<Entity> getEntities();

	default int compareTo(Event event) {
	    return Integer.compare(this.getInstant(), event.getInstant());
    }

    default String print() {
	    StringBuilder sb = new StringBuilder()
                .append("Event: ").append(getClass().getSimpleName()).append('\n')
                .append("Instant: ").append(getInstant()).append('\n');

	    for (Entity entity : getEntities()) {
	        sb.append(entity.getClass().getSimpleName()).append(": ").append(entity).append('\n');
        }

	    return sb.toString();
    }
  
}