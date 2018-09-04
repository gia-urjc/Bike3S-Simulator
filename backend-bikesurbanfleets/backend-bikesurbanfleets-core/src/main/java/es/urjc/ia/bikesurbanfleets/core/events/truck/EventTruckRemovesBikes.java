
package es.urjc.ia.bikesurbanfleets.core.events.truck;

import java.util.ArrayList;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Truck;

public class EventTruckRemovesBikes extends EventTruck {
	private Station originStation;
	private int bikes;
	private List<Entity> entities;
	
	public EventTruckRemovesBikes(int instant, Truck truck, Station station, int bikes) {
		super(instant, truck);
		this.originStation = station;
		this.bikes = bikes;
	}

	public Station getOriginStation() {
		return originStation;
	}

	public int getBikes() {
		return bikes;
	}
		@Override
	public List<Event> execute() {
		List<Event> events = new ArrayList();
		return events;
	}
	
	@Override
	public List<Entity> getEntities() {
		return entities;
	}

}
