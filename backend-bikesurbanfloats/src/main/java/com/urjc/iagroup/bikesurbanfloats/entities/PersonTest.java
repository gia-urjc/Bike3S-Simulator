package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class PersonTest extends Person {
	
	public PersonTest(int id, GeoPoint position) {
		super(id, position);
	}

	public boolean decidesToLeaveSystem() {
		return SystemInfo.random.nextBoolean();
	}
	
	private List<Station> obtainStationsWithoutBikeReservationAttempts() {
		List<Station> stations = new ArrayList<>(SystemInfo.stations);
		for (Station station: getBikeReservationAttempts()) {
			stations.remove(station);
		}
		return stations;
	}
	
	private List<Station> obtainStationsWithoutSlotReservationAttempts() {
		List<Station> stations = new ArrayList<>(SystemInfo.stations); 
		for (Station station: getSlotReservationAttempts()) {
			stations.remove(station);
		}
		return stations;
	}

	public Station determineStationToRentBike() {
		List<Station> stations = obtainStationsWithoutBikeReservationAttempts();
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
			int numberStations = SystemInfo.stations.size();
			int indexStation = SystemInfo.random.nextInt(0,  numberStations - 1);
			destination = SystemInfo.stations.get(indexStation);
		}
		return destination;
	}
	
	public Station determineStationToReturnBike() {
		List<Station> stations = obtainStationsWithoutSlotReservationAttempts();
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
			int numberStations = SystemInfo.stations.size();
			int indexStation = SystemInfo.random.nextInt(0,  numberStations - 1);
			destination = SystemInfo.stations.get(indexStation);
		}
		
		return destination;
	}
	
	public boolean decidesToReserveBike() {
		return SystemInfo.random.nextBoolean();
	}

	public boolean decidesToReserveSlot() {
		return SystemInfo.random.nextBoolean();
	}
	
	public GeoPoint decidesNextPoint() {
		return SystemInfo.rectangle.randomPoint();
	}
	
	public boolean decidesToReturnBike() {
		return SystemInfo.random.nextBoolean();
	}
	
	public boolean decidesToRentBikeAtOtherStation() {
		return SystemInfo.random.nextBoolean();
	}
	
	public void updatePosition(int time) {
		double distance = time * getPosition().distanceTo(getDestinationStation().getPosition()) / timeToReach(getDestinationStation().getPosition());
 		GeoPoint newPoint = getPosition().reachedPoint(distance, getDestinationStation().getPosition());
		setPosition(newPoint);
		
	}

	@Override
	public boolean decidesToDetermineOtherStation() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
