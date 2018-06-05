package es.urjc.ia.bikesurbanfleets.consultSystems;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.Station;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByDistance;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByProportionBetweenDistanceAndBikes;
import es.urjc.ia.bikesurbanfleets.infraestructureEntities.comparators.ComparatorByProportionBetweenDistanceAndSlots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** 
 * @author IAgroup
 *
 */
public class InformationSystem {
	private SystemManager systemManager;
	
    public InformationSystem(SystemManager systemManager) {
    	this.systemManager = systemManager;
    }
    
    private List<Station> validStationsToRentBike(GeoPoint point, int maxDistance, List<Station> stations) {
    	return stations.stream().filter(station -> station.getPosition()
        .distanceTo(point) <= maxDistance && station.availableBikes() > 0)
           .collect(Collectors.toList());
    }
    
    private List<Station> validStationsToRentBike(List<Station> stations) {
    	return stations.stream().filter(station -> station.availableBikes() > 0)
           .collect(Collectors.toList());
    }

    private List<Station> validStationsToReturnBike(List<Station> stations) {
    	return stations.stream().filter( station ->	station.availableSlots() > 0)
         .collect(Collectors.toList());
    }

    /**
     * It recommends stations by the nunmber of available bikes they have: first, it recommends 
     * those which have the most bikes available and finally, those with the least bikes available.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the number of 
     * available bikes. 
     * @return a list of stations ordered descending by the number of available bikes.
     */
    public List<Station> recommendByNumberOfBikes(GeoPoint point, int maxDistance) {
    	List<Station> stations = systemManager.consultStations();
     Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
     return validStationsToRentBike(point, maxDistance, stations).stream().sorted(byNumberOfBikes).collect(Collectors.toList());
    }
    
    public List<Station> recommendByNumberOfBikes() {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
        return validStationsToRentBike(stations).stream().sorted(byNumberOfBikes).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by the nunmber of available slots they have: first, it recommends 
     * those which have the most slots available and finally, those with the least slots available.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the number of 
     * available slots. 
     * @return a list of stations ordered descending by the number of available slots.
     */
    public List<Station> recommendByNumberOfSlots() {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s2.availableSlots(), s1.availableSlots());
        return validStationsToReturnBike(stations).stream().sorted(byNumberOfSlots).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by a factor which consists of the quotient between the distance 
     * from each station to the specified geographical point and the number of available bikes 
     * the station contains: first, it recommends those stations which have the smallest proportion 
     * and finally, those with the greatest one (the smallest the quotient, the better the station).
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the previosuly 
     * described proportion (distance divided by number of available bikes). 
     * @return a list of stations ordered asscending by the previously described proportion.  
     */
    public List<Station> recommendByProportionBetweenDistanceAndBikes(GeoPoint point, int maxDistance) {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byProportion = new ComparatorByProportionBetweenDistanceAndBikes(point);
        return validStationsToRentBike(point, maxDistance, stations)
        		.stream().sorted(byProportion).collect(Collectors.toList());
    }
    
    public List<Station> recommendByProportionBetweenDistanceAndBikes(GeoPoint point) {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byProportion = new ComparatorByProportionBetweenDistanceAndBikes(point);
        return validStationsToRentBike(stations)
        		.stream().sorted(byProportion).collect(Collectors.toList());
        }
    
    /**
     * It recommends stations by a factor which consists of the quotient between the distance 
     * from each station to the specified geographical point and the number of available slots
     * the station contains: first, it recommends those stations which have the smallest proportion 
     * and finally, those with the greatest one (the smallest the quotient, the better the station).
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the previosuly 
     * described proportion (distance divided by number of available slots). 
     * @return a list of stations ordered asscending by the previously described proportion.  
     */
    public List<Station> recommendByProportionBetweenDistanceAndSlots(GeoPoint point) {
    	List<Station> stations = systemManager.consultStations();
    	  Comparator<Station> byProportion = new ComparatorByProportionBetweenDistanceAndSlots(point);
          return validStationsToReturnBike(stations)
          		.stream().sorted(byProportion).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by the distance (linear or real depending on a global configuration 
     * parameter) they are from the specified geographical point: first, it recommends 
     * those which are closest to the point and finally, those wich are the most 
     * distant to taht same point.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the linear distance 
     * between them and the specified geographical point.  
     * @return a list of stations ordered asscending by the linear distance from them to 
     * the specified geographical point.
     */
    public List<Station> recommendToRentBikeByDistance(GeoPoint point, int maxDistance) {
    	List<Station> stations = systemManager.consultStations();
     Comparator<Station> byDistance = new ComparatorByDistance(point);
        return validStationsToRentBike(point, maxDistance, stations)
        		.stream().sorted(byDistance).collect(Collectors.toList());
    }
    
    public List<Station> recommendToRentBikeByDistance(GeoPoint point) {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byDistance = new ComparatorByDistance(point);
        return validStationsToRentBike(stations)
        		.stream().sorted(byDistance).collect(Collectors.toList());
    }

    public List<Station> recommendToReturnBikeByDistance(GeoPoint point) {
    	List<Station> stations = systemManager.consultStations();
        Comparator<Station> byDistance =new ComparatorByDistance(point);
        return validStationsToReturnBike(stations)
        		.stream().sorted(byDistance).collect(Collectors.toList());
    }
    
    
}