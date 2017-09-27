package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class PersonTest extends Person {

	
	public PersonTest(GeoPoint position) {
		super(position);
	}

	@Override
	public Station determineDestination() {
		ArrayList<Station> stations = ConfigInfo.stations;
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint personGeoPoint = currentStation.getPosition();
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
	public boolean wantsToGoDirectlyToStation() {
		Random random = new Random();
	    return random.nextBoolean();
	}

}
