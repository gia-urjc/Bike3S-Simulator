package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.core.RectangleSimulation;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

public class PersonTest extends Person {
	private static final RandomUtil random = new RandomUtil();
	private static final RectangleSimulation rectangleSimulator = SystemInfo.rectangle;
	
	public PersonTest(int id, GeoPoint position) {
		super(id, position);
	}
	
	public PersonTest(PersonTest personTest) {
		super(personTest);
	}
	
	@Override
	public boolean decidesToLeaveSystem() {
		return random.nextBoolean();
	}

	@Override
	public Station determineStation() {
		ArrayList<Station> stations = SystemInfo.stations;
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint personGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(personGeoPoint);
			if(distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			throw new IllegalStateException("There's no stations in this configuration");
		}
		return destination;
	}

	@Override
	public boolean decidesToReserveBike(Station station) {
		boolean decidesToReserve = random.nextBoolean(); 

		if (decidesToReserve) {
			reservesBike(station);
		}
		return decidesToReserve;
	}

	@Override
	public boolean decidesToReserveSlot(Station station) {
		boolean decidesToReserve = random.nextBoolean();
		if (decidesToReserve) {
			reservesSlot(station);
		}
		return decidesToReserve;	
		}

	@Override
	public GeoPoint decidesNextPoint() {
		return rectangleSimulator.randomPoint();
	}
	
	@Override
	public boolean decidesToReturnBike() {
		return random.nextBoolean();
	}
	
	public void updatePosition(int time) {
		double distance = time * getPosition().distanceTo(getDestinationStation().getPosition()) / timeToReach(getDestinationStation().getPosition());
 		GeoPoint newPoint = getPosition().reachedPoint(distance, getDestinationStation().getPosition());
		setPosition(newPoint);
		
	}
	
}
