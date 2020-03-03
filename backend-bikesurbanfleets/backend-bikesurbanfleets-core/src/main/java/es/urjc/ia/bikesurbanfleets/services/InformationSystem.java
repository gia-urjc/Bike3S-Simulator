package es.urjc.ia.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.services.graphManager.GraphManager;
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
    private GraphManager graphManager;
	
    public InformationSystem(StationManager infraestructureManager, GraphManager graphManager) {
    	this.stationManager = infraestructureManager;
        this.graphManager=graphManager;
    }
        
    public List<Station> getStationsWithAvailableBikesOrderedByWalkDistance(GeoPoint point) {
    	List<Station> stations = stationManager.consultStations();
        return stations.stream().filter(station -> station.availableBikes() > 0)
        		.sorted(StationComparator.byDistance(point, graphManager, "foot")).collect(Collectors.toList());
    }

    public List<Station> getAllStationsOrderedByDistance(GeoPoint point, String vehicle) {
        List<Station> stations = stationManager.consultStations();
        return stations.stream()
                .sorted(StationComparator.byDistance(point,graphManager, vehicle)).collect(Collectors.toList());
    }

    public List<Station> getStationsWithAvailableSlotsOrderedByDistance(GeoPoint point, String vehicle) {
    	List<Station> stations = stationManager.consultStations();
     return stations.stream().filter(station -> station.availableSlots() > 0)
        		.sorted(StationComparator.byDistance(point,graphManager, vehicle)).collect(Collectors.toList());
    }
    
    public List<Station> getAllStations() {
    	return stationManager.consultStations(); 
    }
    
    
}