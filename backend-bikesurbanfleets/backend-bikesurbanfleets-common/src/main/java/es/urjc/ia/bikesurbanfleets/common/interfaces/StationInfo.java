package es.urjc.ia.bikesurbanfleets.common.interfaces;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

public interface StationInfo {
	int getCapacity();
	GeoPoint getPosition();
	int availableBikes();
	int getReservedBikes();
	int getReservedSlots();
}
