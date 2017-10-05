package com.urjc.iagroup.bikesurbanfloats.events;

import java.util.List;

public abstract class Event implements Comparable<Event> {
    private int instant;

    public Event(int instant) {
        this.instant = instant;
    }

    public int getInstant() {
        return instant;
    }

    // TODO: is this necessary?
    public void setInstant(int instant) {
        this.instant = instant;
    }

    public abstract List<Event> execute();

    public int compareTo(Event event) {
        return Integer.compare(this.instant, event.instant);
    }
    
    public String toString() {
    	return "Event: "+getClass().getSimpleName() +"\nInstant: "+instant+"\n";
    }

}