package es.urjc.ia.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.simple.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 
 * @author IAgroup
 *
 */
public class InformationSystem {

    private StationManager stationManager;
	
    public InformationSystem(StationManager infraestructureManager) {
    	this.stationManager = infraestructureManager;
    }
        
    public List<Station> getStationsWithAvailableBikesOrderedByDistance(GeoPoint point) {
    	List<Station> stations = stationManager.consultStations();
        return stations.stream().filter(station -> station.availableBikes() > 0)
        		.sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getAllStationsOrderedByDistance(GeoPoint point) {
        List<Station> stations = stationManager.consultStations();
        return stations.stream()
                .sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getStationsWithAvailableSlotsOrderedByDistance(GeoPoint point) {
    	List<Station> stations = stationManager.consultStations();
     return stations.stream().filter(station -> station.availableSlots() > 0)
        		.sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }
    
    public List<Station> getAllStations() {
    	return stationManager.consultStations(); 
    }
    
    
}