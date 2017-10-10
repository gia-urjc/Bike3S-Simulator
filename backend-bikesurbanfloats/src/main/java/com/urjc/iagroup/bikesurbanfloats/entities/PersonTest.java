package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class PersonTest extends Person implements PersonSpecificBehaviour {
	
	public PersonTest(int id, GeoPoint position) {
		super(id, position);
	}
	
	public PersonTest(PersonTest personTest) {
		super(personTest);
	}

	public boolean decidesToLeaveSystem() {
		return random.nextBoolean();
	}

	public Station determineStation() {
		ArrayList<Station> stations = SystemInfo.stations;
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint personGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(personGeoPoint);
			if(!personGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			throw new IllegalStateException("There's no stations in this configuration");
		}
		return destination;
	}
	
	public boolean decidesToReserveBike(Station station) {
		boolean decidesToReserve = random.nextBoolean(); 

		if (decidesToReserve) {
			reservesBike(station);
		}
		return decidesToReserve;
	}

	public boolean decidesToReserveSlot(Station station) {
		boolean decidesToReserve = random.nextBoolean();
		if (decidesToReserve) {
			reservesSlot(station);
		}
		return decidesToReserve;	
		}
	
	public GeoPoint decidesNextPoint() {
		return rectangle.randomPoint();
	}
	
	public boolean decidesToReturnBike() {
		return random.nextBoolean();
	}
	
	public boolean decidesToRentBikeAtOtherStation() {
		return random.nextBoolean();
	}
	
	public void updatePosition(int time) {
		double distance = time * getPosition().distanceTo(getDestinationStation().getPosition()) / timeToReach(getDestinationStation().getPosition());
 		GeoPoint newPoint = getPosition().reachedPoint(distance, getDestinationStation().getPosition());
		setPosition(newPoint);
		
	}
	
}
