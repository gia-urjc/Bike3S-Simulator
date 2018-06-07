package es.urjc.ia.bikesurbanfleets.consultSystems;

import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public abstract class RecommendationSystem {
	private InfraestructureManager infraestructureManager;
	
	public abstract List<StationInfo> recommendStationToRentBike(GeoPoint	point);
 public abstract List<StationInfo> recommendStationToReturnBike(GeoPoint point);
}
