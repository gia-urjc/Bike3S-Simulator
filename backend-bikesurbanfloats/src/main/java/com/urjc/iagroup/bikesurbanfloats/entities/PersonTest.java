package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.List;
import java.util.stream.Collectors;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.ReservationType;

public class PersonTest extends Person {
	
	public PersonTest(int id, GeoPoint position) {
		super(id, position);
	}

	public boolean decidesToLeaveSystem() {
		return SystemInfo.random.nextBoolean();
	}
	
	private List<Station> obtainStationsWithBikeReservationAttempts(int instant) {
 		List<Reservation> reservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.BIKE && 
 		reservation.getSuccessful() == false && reservation.getInstant() == instant).collect(Collectors.toList());
 		List<Station> stations = reservations.stream().map(Reservation::getStation).collect(Collectors.toList());		
 		return stations;
	}
	
	private List<Station> obtainStationsWithSlotReservationAttempts(int instant) {
 		List<Reservation> reservations = getReservations().stream().filter(reservation -> reservation.getType() == ReservationType.SLOT && 
 				reservation.getSuccessful() == false && reservation.getInstant() == instant).collect(Collectors.toList());
 		List<Station> stations = reservations.stream().map(Reservation::getStation).collect(Collectors.toList());		
 		return stations;
	}

	public Station determineStationToRentBike(int instant) {
		List<Station> stations = obtainStationsWithBikeReservationAttempts(instant);
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
	
	public Station determineStationToReturnBike(int instant) {
		List<Station> stations = obtainStationsWithSlotReservationAttempts(instant);
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
