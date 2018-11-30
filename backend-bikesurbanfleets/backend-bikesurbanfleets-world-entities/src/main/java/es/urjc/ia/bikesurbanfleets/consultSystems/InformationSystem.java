package es.urjc.ia.bikesurbanfleets.consultSystems;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.util.List;
import java.util.stream.Collectors;

/** 
 * @author IAgroup
 *
 */
public class InformationSystem {

	private InfraestructureManager infraestructureManager;
	
    public InformationSystem(InfraestructureManager infraestructureManager) {
    	this.infraestructureManager = infraestructureManager;
    }
    
    private List<Station> stationsWithAvailableBikes(List<Station> stations) {
    	return stations.stream().filter(station -> station.availableBikes() > 0)
           .collect(Collectors.toList());
    }
    
    private List<Station> stationsWithAvailableSlots(List<Station> stations) {
    	return stations.stream().filter( station ->	station.availableSlots() > 0)
            .collect(Collectors.toList());
    }
    
    public List<Station> getStationsWithAvailableBikesOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
     return stationsWithAvailableBikes(stations)
        		.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getAllStationsOrderedByDistance(GeoPoint point) {
        List<Station> stations = infraestructureManager.consultStations();
        return stations.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getStationsWithAvailableSlotsOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
     return stationsWithAvailableSlots(stations)
        		.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }
    
    public List<Station> getAllStations() {
    	return infraestructureManager.consultStations(); 
    }
    
    
}