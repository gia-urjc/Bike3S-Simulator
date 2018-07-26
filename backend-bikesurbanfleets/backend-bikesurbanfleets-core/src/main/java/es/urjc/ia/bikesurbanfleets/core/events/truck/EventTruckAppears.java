package es.urjc.ia.bikesurbanfleets.core.events.truck;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.events.Event;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Truck;

public class EventTruckAppears extends EventTruck {
	private GeoPoint position;
	
	public EventTruckAppears(int instant, Truck truck, GeoPoint position) {
		super(instant, truck);
		this.position = position;
	}
	
	@Override
	public List<Event> execute() {
		List<Event> events = new ArrayList<>();
		truck.setPosition(position);
		return events;
	}

}
