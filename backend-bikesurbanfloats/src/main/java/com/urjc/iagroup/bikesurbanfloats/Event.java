package com.urjc.iagroup.bikesurbanfloats;

public abstract class Event implements Comparable<> {
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
	

		
}