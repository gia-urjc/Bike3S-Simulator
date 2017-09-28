package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import com.urjc.iagroup.bikesurbanfloats.config.ConfigInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class PersonTest extends Person {
	private static final ThreadLocalRandom random = ThreadLocalRandom.current();
	
	public PersonTest(int id, GeoPoint position) {
		super(id, position);
	}

	@Override
	public Station determineStation() {
		ArrayList<Station> stations = ConfigInfo.stations;
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
		double latitud = random.nextDouble(-90, 90+1);
		double longitud = random.nextDouble(-180, 180+1);
		return new GeoPoint(latitud, longitud);
	}
	
	@Override
	public boolean decidesToReturnBike() {
		return random.nextBoolean();
	}

}
