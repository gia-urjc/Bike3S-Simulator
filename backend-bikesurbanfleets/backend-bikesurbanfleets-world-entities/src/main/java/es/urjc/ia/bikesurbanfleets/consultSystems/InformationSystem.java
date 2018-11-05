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
    
    private List<Station> validStationInfosToRentBike(List<Station> stations) {
    	return stations.stream().filter(station -> station.availableBikes() > 0).collect(Collectors.toList());
    }
    
    private List<Station> validStationInfosToRentBike(GeoPoint point, int maxDistance, List<Station> stations) {
    	return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= maxDistance)
    			.filter(station -> station.availableBikes() > 0).collect(Collectors.toList());
    }
    
    private List<Station> validStationInfosToReturnBike(List<Station> stations) {
    	return stations.stream().filter(station -> station.availableSlots() > 0).collect(Collectors.toList());
    }
    
    private List<Station> validStationInfosToReturnBike(GeoPoint point, int maxDistance, List<Station> stations) {
    	return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= maxDistance)
    			.filter(station -> station.availableSlots() > 0).collect(Collectors.toList());
    }


    
    
    /**
     * It returns a list of stations ordered with respect to a reference geographical point.
 * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @return a list of stations ordered asscending by the linear distance from them to 
     * the specified geographical point.
     */
    public List<Station> getStationsToRentBikeOrderedByDistance(GeoPoint point, int maxDistance) {
    	List<Station> stations = infraestructureManager.consultStations();
     return validStationInfosToRentBike(point, maxDistance, stations)
        		.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }
    
    public List<Station> getStationsToRentBikeOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
     return validStationInfosToRentBike(stations)
        		.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getStationsBikeOrderedByDistanceNoFiltered(GeoPoint point) {
        List<Station> stations = infraestructureManager.consultStations();
        return stations.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }

    public List<Station> getStationsToReturnBikeOrderedByDistance(GeoPoint point) {
    	List<Station> stations = infraestructureManager.consultStations();
     return validStationInfosToReturnBike(stations)
        		.stream().sorted(StationComparator.byDistance(point)).collect(Collectors.toList());
    }
    
    public List<Station> getStations() {
    	return infraestructureManager.consultStations(); 
    }
    
    
}