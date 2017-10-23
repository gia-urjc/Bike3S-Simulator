package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

public class UserTest extends User {
	
	public UserTest(int id, GeoPoint position, SystemConfiguration systemConfig) {
		super(id, position, systemConfig);
	}

	public boolean decidesToLeaveSystem(int instant) {
		return obtainStationsWithBikeReservationAttempts(instant).size() == systemConfig.getStations().size() ? true : false;
	}
	
	public Station determineStationToRentBike(int instant) {
		List<Station> stations = obtainStationsWithoutBikeReservationAttempts(instant);
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint userGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(userGeoPoint);
			if(!userGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			int numberStations = systemConfig.getStations().size();
			int indexStation = StaticRandom.nextInt(0,  numberStations - 1);
			destination = systemConfig.getStations().get(indexStation);
		}
		return destination;
	}
	
	public Station determineStationToReturnBike(int instant) {
		List<Station> stations = obtainStationsWithoutSlotReservationAttempts(instant);
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint userGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(userGeoPoint);
			if(!userGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			int numberStations = systemConfig.getStations().size();
			int indexStation = StaticRandom.nextInt(0,  numberStations - 1);
			destination = systemConfig.getStations().get(indexStation);
		}
		
		return destination;
	}
	
	public boolean decidesToReserveBike() {
		return StaticRandom.nextBoolean();
	}

	public boolean decidesToReserveSlot() {
		return StaticRandom.nextBoolean();
	}
	
	public GeoPoint decidesNextPoint() {
		return systemConfig.getBoundingBox().randomPoint();
	}
	
	public boolean decidesToReturnBike() {
		return StaticRandom.nextBoolean();
	}
	
	public boolean decidesToRentBikeAtOtherStation() {
		return StaticRandom.nextBoolean();
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
