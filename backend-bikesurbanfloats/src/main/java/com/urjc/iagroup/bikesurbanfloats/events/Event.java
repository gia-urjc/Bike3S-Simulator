package com.urjc.iagroup.bikesurbanfloats.events;

public abstract class Event implements Comparable<Event> {
	private int instant;
	
	public Event() {
		this.instant = 0;
	}
	
	public Event(int instant) {
		this.instant = instant;
	}

	public int getInstant() {
		return instant;
	}

	public void setInstant(int instant) {
		this.instant = instant;
	}
	
	public abstract List<Event> execute();
	
  public int compareTo(Object o) {
	  if (!(o instanceof Event))
		  throw new ClassCastException();
	  Event event=(Event)o;
	  if (this.instant<event.instant)
		  return -1;
	  else if (this.instant>event.instant)
		  return 1;
	  return 0;
  }
		
}