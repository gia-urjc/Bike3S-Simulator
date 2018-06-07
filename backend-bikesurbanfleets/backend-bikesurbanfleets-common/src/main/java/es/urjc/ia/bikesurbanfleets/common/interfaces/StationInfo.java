package es.urjc.ia.bikesurbanfleets.common.interfaces;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

public interface StationInfo {
	int getId();
	int getCapacity();
	GeoPoint getPosition();
	int availableBikes();
	int availableSlots();
	int getReservedBikes();
	int getReservedSlots();
}
