package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import java.util.List;
/**
 * This interface provides the common behaviour of events.
 * Also, it provides default implementations for some methods.
 * @author IAgroup
 *
 */
public interface Event extends Comparable<Event> {
	/**
	 * @return the time instant when the event will ocurr.
	 */
	int getInstant();
	
	/**
	 * It proccesses the event so that the relevant changes at the system occur.
	 * @return a list of generated events as a consequence of event execution.
	 * @throws Exception
	 */
	List<Event> execute() throws Exception;
	
	/**
	 * @return a list with all the entities involved in the event.
	 */
	List<Entity> getEntities();
	
	/**
	 * It allows to compare 2 events by the time instant they'll occur. 
	 */
	default int compareTo(Event event) {
	    return Integer.compare(this.getInstant(), event.getInstant());
    }
	
	/**
	 * @return a string with the event information.
	 */
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