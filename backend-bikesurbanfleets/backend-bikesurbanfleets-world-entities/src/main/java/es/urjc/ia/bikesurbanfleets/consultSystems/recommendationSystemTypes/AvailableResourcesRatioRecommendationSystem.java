package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.SystemManager;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByDistance;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByProportionBetweenDistanceAndBikes;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByProportionBetweenDistanceAndSlots;

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
public class AvailableResourcesRatioRecommendationSystem extends RecommendationSystem {

    /**
     * It is the maximum distance in meters between the recommended stations and the indicated 
     * geographical point.
     */
	private final int MAX_DISTANCE = 800; 

    /** 
     * It indicates the number of stations to consider when choosing one randomly in recommendation by ratio between available resources and station capacity.
     */
    private final int N_STATIONS = 5;
    
    private SystemManager systemManager;
    
    public AvailableResourcesRatioRecommendationSystem(SystemManager systemManager) {
        this.systemManager = systemManager;
    }

    /**
     * It verifies which stations are less than MAX_DISTANCE meters in a straight line from  
     * * the user position 
     * @param point It's the user current position 
     * @return an unordered list of stations which are nearer than MAX_DISTANCE meters from the user
     */
     private List<Station> validStationsToRentBike(List<Station> stations) {
    	return stations.stream().filter(station -> station.availableBikes() > 0)
           .collect(Collectors.toList());
    }

    private List<Station> validStationsToReturnBike(List<Station> stations) {
    	return stations.stream().filter( (station) ->	station.availableSlots() > 0)
         .collect(Collectors.toList());
    }
    
    private List<Station> fartherStations(GeoPoint point, List<Station> stations) {
    	return stations.stream().filter( station -> station.getPosition().distanceTo(point) > MAX_DISTANCE)
    			.collect(Collectors.toList());
    }

    private List<Station> nearerStations(GeoPoint point, List<Station> stations) {
    	return stations.stream().filter( station -> station.getPosition().distanceTo(point) <= MAX_DISTANCE)
    			.collect(Collectors.toList());
    }
    
    private List<Station> rebalanceWhenRenting(List<Station> stations) {
    	double ratioSum = 0.0;
    	int i;
    	for (i=0; i<N_STATIONS; i++) {
    		ratioSum += stations.get(i).availableBikes() / stations.get(i).getCapacity();
    	}
    	
    	double random = systemManager.getRandom().nextDouble(0, ratioSum);
    	double ratio;
    	for (i=0; i<N_STATIONS; i++) {
    		ratio = stations.get(i).availableBikes() / stations.get(i).getCapacity();
    		if (random <= ratio) {
    			break;
    		}
    		random -= ratio;
    	}
    	Station selected = stations.remove(i);
    	stations.add(0, selected);
    	return stations;
    }
    
    private List<Station> rebalanceWhenReturning(List<Station> stations) {
    	double ratioSum = 0.0;
    	int i;
    	for (i=0; i<N_STATIONS; i++) {
    		ratioSum += stations.get(i).availableSlots() / stations.get(i).getCapacity();
    	}
    	
    	double random = systemManager.getRandom().nextDouble(0, ratioSum);
    	double ratio;
    	for (i=0; i<N_STATIONS; i++) {
    		ratio = stations.get(i).availableSlots() / stations.get(i).getCapacity();
    		if (random <= ratio) {
    			break;
    		}
    		random -= ratio;
    	}
    	Station selected = stations.remove(i);
    	stations.add(0, selected);
    	return stations;
    }

    public List<Station> recommendToRent(GeoPoint point) {
    	List<Station> stations = validStationsToRentBike(systemManager.consultStations());
    	List<Station> nearer = nearerStations(point, stations);
    	List<Station> farther = fartherStations(point, stations);
    	
    	Comparator<Station> byBikesRatio = (s1, s2) -> Double.compare((double)s2.availableBikes()/(double)s2
    			.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
    	
     nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
     farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());
     
     nearer.addAll(farther);
     return rebalanceWhenRenting(stations);
    }
 
    public List<Station> recommendToReturn(GeoPoint point) {
    	List<Station> stations = validStationsToReturnBike(systemManager.consultStations());
    	List<Station> nearer = nearerStations(point, stations);
    	List<Station> farther = fartherStations(point, stations);
    	
    	Comparator<Station> byBikesRatio = (s1, s2) -> Double.compare((double)s2.availableSlots()/(double)s2
    			.getCapacity(), (double)s1.availableBikes()/(double)s1.getCapacity());
    	
     nearer = nearer.stream().sorted(byBikesRatio).collect(Collectors.toList());
     farther = farther.stream().sorted(byBikesRatio).collect(Collectors.toList());
     
     nearer.addAll(farther);
     return rebalanceWhenReturning(stations);
    }
   
}