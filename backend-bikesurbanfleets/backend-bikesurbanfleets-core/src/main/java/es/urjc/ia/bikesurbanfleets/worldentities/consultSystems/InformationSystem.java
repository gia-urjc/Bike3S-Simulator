package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 
 * @author IAgroup
 *
 */
public class InformationSystem {

    private InfraestructureManager infraestructureManager;
	
    public InformationSystem(InfraestructureManager infraestructureManager) {
    	this.infraestructureManager = infraestructureManager;
    }
        
    public List<Station> getStationsWithAvailableBikesOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
        return stations.stream().filter(station -> station.availableBikes() > 0)
        		.sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getAllStationsOrderedByDistance(GeoPoint point) {
        List<Station> stations = infraestructureManager.consultStations();
        return stations.stream()
                .sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getStationsWithAvailableSlotsOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
     return stations.stream().filter(station -> station.availableSlots() > 0)
        		.sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }
    
    public List<Station> getAllStations() {
    	return infraestructureManager.consultStations(); 
    }
    
    
}