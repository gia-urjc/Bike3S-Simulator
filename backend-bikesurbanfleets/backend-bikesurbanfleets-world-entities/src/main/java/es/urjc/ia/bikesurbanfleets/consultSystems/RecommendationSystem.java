package es.urjc.ia.bikesurbanfleets.consultSystems;

import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;

public abstract class RecommendationSystem {
	private SystemManager systemManager;
	
	public abstract List<Station> recommendToRent(GeoPoint	point);
 public abstract List<Station> recommendToReturn(GeoPoint point);
}
