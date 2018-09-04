
package es.urjc.ia.bikesurbanfleets.core.events.truck;

import java.util.List;

public class EventTruckRemovesBikes extends EventTruck {
	private Station originStation;
	private int bikes;
	
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
		
	}

}
