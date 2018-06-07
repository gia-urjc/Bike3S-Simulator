package es.urjc.ia.bikesurbanfleets.common.interfaces;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;

public interface UserInfo {
	int getId();
	GeoPoint getPosition();
	double getAverageVelocity();
		boolean hasReservedBike(); 
	boolean hasReservedSlot();
	ReservationInfo consultReservation();
	BikeInfo consultBike();
	StationInfo consultDestinationStation();
}
