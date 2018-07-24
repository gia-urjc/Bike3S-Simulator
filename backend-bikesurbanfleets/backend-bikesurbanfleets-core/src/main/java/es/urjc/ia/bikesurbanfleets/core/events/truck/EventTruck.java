package es.urjc.ia.bikesurbanfleets.core.events.truck;

import java.util.ArrayList;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Truck;

public abstract class EventTruck implements Event {
	private int instant;
	protected Truck truck;
		
	public EventTruck(int instant, Truck truck) {
		this.instant = instant;
		this.truck = truck;
	}
	
	public int getInstant() {
		return this.instant;
	}
	
	public abstract List<Entity> getEntities();
	
	public abstract List<Event> execute();

}
