package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems;

import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public abstract class RecommendationSystem {
	
	/**
     * It provides information about the infraestructure state.
     */
   protected InfraestructureManager infraestructureManager;
   
   /**
    * It filters stations which have not available bikes.  
    * @return a list of stations with available bikes.
    */
    protected List<Station> validStationsToRentBike(List<Station> stations) {
   	return stations.stream().filter(station -> station.availableBikes() > 0)
          .collect(Collectors.toList());
   }
    
    /**
     * It filters stations which have not available bikes.  
     * @return a list of stations with available bikes.
     */
   protected List<Station> validStationsToReturnBike(List<Station> stations) {
   	return stations.stream().filter( (station) ->	station.availableSlots() > 0)
        .collect(Collectors.toList());
   }
   
 
   public RecommendationSystem(InfraestructureManager infraestructureManager) {
	   this.infraestructureManager = infraestructureManager; 	
   }
	
	public abstract List<Recommendation> recommendStationToRentBike(GeoPoint	point);
 	public abstract List<Recommendation> recommendStationToReturnBike(GeoPoint point);


}
