package es.urjc.ia.bikesurbanfleets.common.interfaces;

public interface ReservationInfo {
	int getId(); 
	int getStartInstant();
	int getEndInstant();
	//ReservationType getType();
	//ReservationState getState();
	StationInfo consultStation();
	UserInfo consultUser();
	BikeInfo consultBike();
	}
