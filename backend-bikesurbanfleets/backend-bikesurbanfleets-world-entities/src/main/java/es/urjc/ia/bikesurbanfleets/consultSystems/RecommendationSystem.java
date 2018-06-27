package es.urjc.ia.bikesurbanfleets.consultSystems;

import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;

public abstract class RecommendationSystem {
	
	/**
     * It provides information about the infraestructure state.
     */
   protected InfraestructureManager infraestructureManager;
   
   public RecommendationSystem(InfraestructureManager infraestructureManager) {
	   this.infraestructureManager = infraestructureManager; 	
   }
	
	public abstract List<StationInfo> recommendStationToRentBike(GeoPoint	point);
 	public abstract List<StationInfo> recommendStationToReturnBike(GeoPoint point);


}
