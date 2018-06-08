package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** 
 * This class is a system which recommends the user the stations to which he should go to contribute with system rebalancing.  
 * Then, this recommendation system gives the user a list of stations ordered descending by the "resources/capacityº" ratio. 
 *    
 * @author IAgroup
 *
 */
public class RecommendationSystemByAvailableResourcesRatio extends RecommendationSystem {

    /**
     * It is the maximum distance in meters between the recommended stations and the indicated 
     * geographical point.
     */
	private int maxDistance = 800;

    /** 
     * It indicates the number of stations to consider when choosing one randomly in recommendation by ratio between available resources and station capacity.
     */
    private final int N_STATIONS = 5;
    
    /**
     * It provides information about the infraestructure state.
     */
    private InfraestructureManager infraestructure;
    
    /**
     * It contains several comparators to sort stations.
     */
    private StationComparator stationComparator;
    
    public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager) {
        this.infraestructure = infraestructureManager;
    }

    public RecommendationSystemByAvailableResourcesRatio(InfraestructureManager infraestructureManager, Integer maxDistance) {
    	this.infraestructure = infraestructureManager;
    	this.maxDistance = maxDistance;
	}

    /**
     * It verifies which stations are less than MAX_DISTANCE meters in a straight line from  
     * * the user position
     * @return an unordered list of stations which are nearer than MAX_DISTANCE meters from the user
     */
     private List<StationInfo> validStationsToRentBike(List<StationInfo> stations) {
    	return stations.stream().filter(station -> station.availableBikes() > 0)
           .collect(Collectors.toList());
    }

    private List<StationInfo> validStationsToReturnBike(List<StationInfo> stations) {
    	return stations.stream().filter( (station) ->	station.availableSlots() > 0)
         .collect(Collectors.toList());
    }
    
    private List<StationInfo> fartherStations(GeoPoint point, List<StationInfo> stations) {
    	return stations.stream().filter( station -> station.getPosition().distanceTo(point) > maxDistance)
    			.collect(Collectors.toList());
    }

    private List<StationInfo> nearerStations(GeoPoint point, List<StationInfo> stations) {
    	return stations.stream().filter( station -> station.getPosition().distanceTo(point) <= maxDistance)
    			.collect(Collectors.toList());
    }
    
    private List<StationInfo> rebalanceWhenRenting(List<StationInfo> stations) {
    	double ratioSum = 0.0;
    	int i;
    	for (i=0; i<N_STATIONS; i++) {
    		ratioSum += stations.get(i).availableBikes() / stations.get(i).getCapacity();
    	}
    	
    	double random = infraestructure.getRandom().nextDouble(0, ratioSum);
    	double ratio;
    	for (i=0; i<N_STATIONS; i++) {
    		ratio = stations.get(i).availableBikes() / stations.get(i).getCapacity();
    		if (random <= ratio) {
    			break;
    		}
    		random -= ratio;
    	}
    	StationInfo selected = stations.remove(i);
    	stations.add(0, selected);
    	return stations;
    }
    
    private List<StationInfo> rebalanceWhenReturning(List<StationInfo> stations) {
    	double ratioSum = 0.0;
    	int i;
    	for (i=0; i<N_STATIONS; i++) {
    		ratioSum += stations.get(i).availableSlots() / stations.get(i).getCapacity();
    	}
    	
    	double random = infraestructure.getRandom().nextDouble(0, ratioSum);
    	double ratio;
    	for (i=0; i<N_STATIONS; i++) {
    		ratio = stations.get(i).availableSlots() / stations.get(i).getCapacity();
    		if (random <= ratio) {
    			break;
    		}
    		random -= ratio;
    	}
    	StationInfo selected = stations.remove(i);
    	stations.add(0, selected);
    	return stations;
    }
    
    @Override
    public List<StationInfo> recommendStationToRentBike(GeoPoint point) {
    	List<StationInfo> stations = validStationsToRentBike(infraestructure.consultStations());
    	List<StationInfo> nearer = nearerStations(point, stations);
    	List<StationInfo> farther = fartherStations(point, stations);
    	
    	Comparator<StationInfo> byBikesRatio = stationComparator.byBikesCapacityRatio(); 
     nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
     farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());
     
     nearer.addAll(farther);
     return rebalanceWhenRenting(stations);
    }
 
    public List<StationInfo> recommendStationToReturnBike(GeoPoint point) {
    	List<StationInfo> stations = validStationsToReturnBike(infraestructure.consultStations());
    	List<StationInfo> nearer = nearerStations(point, stations);
    	List<StationInfo> farther = fartherStations(point, stations);
    	
    	Comparator<StationInfo> bySlotsRatio = stationComparator.bySlotsCapacityRatio(); 
     nearer = nearer.stream().sorted(bySlotsRatio).collect(Collectors.toList());
     farther = farther.stream().sorted(bySlotsRatio).collect(Collectors.toList());
     
     nearer.addAll(farther);
     return rebalanceWhenReturning(stations);
    }
   
}