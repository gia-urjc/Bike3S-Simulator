package es.urjc.ia.bikesurbanfleets.users;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** 
 * This class is a system which recommends the user the stations that, with respect to a 
 * geographical point, are less than a certain preset distance.
 * The geographical point may be the user position, if he wants to rent or to return a bike 
 * in the closest station to himself, or a place he is going to reach, if he wants to return 
 * the bike in the closest station to that place.    
 * Then, this system gives the user a list of stations ordered ascending or descending (depending 
 *    
 * @author IAgroup
 *
 */
public class RecommendationSystem {
    
    /**
     * It is the maximum distance in meters between the recommended stations and the indicated 
     * geographical point.
     */
    private final int MAX_DISTANCE = 5000;
    
    /**
     * It alloows to manage routes. 
     */
    private GraphManager graph;
    
    public RecommendationSystem(GraphManager graph) {
        this.graph = graph;
    }

    /**
     * It verifies which stations are less than MAX_DISTANCE meters in a straight line from 
     * the indicated geographical point. 
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the initial set of stations within it has to filter those 
     * that don't exceed the preset maximum distance from the specified geographical point.      
     * @return an unordered list of stations from which the system will prepare its recommendations.
     */
    private List<Station> validStationsToRentBikeByLinearDistance(GeoPoint point, List<Station> stations) {
        return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= MAX_DISTANCE 
        		&& station.availableBikes() > 0).collect(Collectors.toList());
    }
    
    private List<Station> validStationsToReturnBikeByLinearDistance(GeoPoint point, List<Station> stations) {
        return stations.stream().filter(station -> station.getPosition().distanceTo(point) <= MAX_DISTANCE 
        		&& station.availableSlots() > 0).collect(Collectors.toList());
    }
 
    /**
     * It verifies which stations have real routes of less than MAX_DISTANCE meters 
     * to the indicated geographical point. 
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the initial set of stations within it has to filter those 
     * that don't exceed the preset maximum distance from the specified geographical point.      
     * @return an unordered list of stations from which the system will prepare its recommendations.
     */
    private List<Station> validStationsToRentBikeByRealRouteDistance(GeoPoint point, List<Station> stations) {
        List<Station> validStations = new ArrayList<>();
        
        for(Station station: stations) {
            List<GeoRoute> routes;
            
            try {
                routes = graph.obtainAllRoutesBetween(station.getPosition(), point);
            }
            catch(Exception e) {
                continue;
            }
            
            List<GeoRoute> validRoutes = routes.stream().filter(route -> route.getTotalDistance() <= MAX_DISTANCE)
                    .collect(Collectors.toList());
            
            if (!validRoutes.isEmpty() && station.availableBikes() > 0) {
                validStations.add(station);
            }
        }
        return validStations;
    }
    
    private List<Station> validStationsToReturnBikeByRealRouteDistance(GeoPoint point, List<Station> stations) {
        List<Station> validStations = new ArrayList<>();
        
        for(Station station: stations) {
            List<GeoRoute> routes;
            
            try {
                routes = graph.obtainAllRoutesBetween(station.getPosition(), point);
            }
            catch(Exception e) {
                continue;
            }
            
            List<GeoRoute> validRoutes = routes.stream().filter(route -> route.getTotalDistance() <= MAX_DISTANCE)
                    .collect(Collectors.toList());
            
            if (!validRoutes.isEmpty() && station.availableSlots() > 0) {
                validStations.add(station);
            }
        }
        return validStations;
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
    public List<Station> recommendByNumberOfBikes(GeoPoint point, List<Station> stations) {
        Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
        return validStationsToRentBikeByLinearDistance(point, stations).stream()
        		.sorted(byNumberOfBikes).collect(Collectors.toList());
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
    public List<Station> recommendByNumberOfSlots(GeoPoint point, List<Station> stations) {
        Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s2.availableSlots(), s1.availableSlots());
        return validStationsToReturnBikeByLinearDistance(point, stations).stream()
        		.sorted(byNumberOfSlots).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by the linear distance they are from the specified geographical 
     * point: first, it recommends those which are closest to the point and finally,
     * those wich are the most distant to taht same point.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the linear distance 
     * between them and the specified geographical point.  
     * @return a list of stations ordered asscending by the linear distance from them to 
     * the specified geographical point.
     */
    public List<Station> recommendToRentBikeByLinearDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byLinearDistance = (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point),
                s2.getPosition().distanceTo(point));
        List<Station> recommendedStations = validStationsToRentBikeByLinearDistance(point, stations)
        		.stream().sorted(byLinearDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }
    
    public List<Station> recommendToReturnBikeByLinearDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byLinearDistance = (s1, s2) -> Double.compare(s1.getPosition().distanceTo(point),
                s2.getPosition().distanceTo(point));
        List<Station> recommendedStations = validStationsToReturnBikeByLinearDistance(point, stations)
        		.stream().sorted(byLinearDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
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
    public List<Station> recommendByProportionBetweenDistanceAndBikes(GeoPoint point, List<Station> stations) {
        Comparator<Station> byProportion = (s1, s2) -> {
        			 double smallestProportionS1 = Double.MAX_VALUE;
            double smallestProportionS2 = Double.MIN_VALUE;
            try {
        					smallestProportionS1 = graph.obtainShortestRouteBetween(s1.getPosition(), point)
        							.getTotalDistance()/s1.availableBikes();
        					smallestProportionS2 = graph.obtainShortestRouteBetween(s2.getPosition(), point)
        							.getTotalDistance()/s2.availableBikes();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
            return Double.compare(smallestProportionS1, smallestProportionS2);
        }; 
        List<Station> recommendedStations = validStationsToRentBikeByRealRouteDistance(point, stations)
        		.stream().sorted(byProportion).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
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
    public List<Station> recommendByProportionBetweenDistanceAndSlots(GeoPoint point, List<Station> stations) {
        Comparator<Station> byProportion = (s1, s2) -> {
        		  double smallestProportionS1 = Double.MAX_VALUE;
        			 double smallestProportionS2 = Double.MIN_VALUE;
        				try {
        						smallestProportionS1 = graph.obtainShortestRouteBetween(s1.getPosition(), point)
        									.getTotalDistance()/s1.availableSlots();
        							smallestProportionS2 = graph.obtainShortestRouteBetween(s2.getPosition(), point)
        										.getTotalDistance()/s2.availableSlots();
        				} catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
        							e.printStackTrace();
        				}
        				return Double.compare(smallestProportionS1, smallestProportionS2);
        }; 
        List<Station> recommendedStations = validStationsToReturnBikeByRealRouteDistance(point, stations)
       					.stream().sorted(byProportion).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }
    
    public List<Station> recommendToRentBikeByRealRouteDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byRealRouteDistance = (s1, s2) -> {
            double shortestRouteS1 = Double.MAX_VALUE;
            double shortestRouteS2 = Double.MIN_VALUE;
            try {
                shortestRouteS1 = graph.obtainShortestRouteBetween(s1.getPosition(), point).getTotalDistance();
                shortestRouteS2 = graph.obtainShortestRouteBetween(s2.getPosition(), point).getTotalDistance();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
            return Double.compare(shortestRouteS1, shortestRouteS2);
        };
        List<Station> recommendedStations = validStationsToRentBikeByRealRouteDistance(point, stations)
        		.stream().sorted(byRealRouteDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }
    
    public List<Station> recommendToReturnBikeByRealRouteDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byRealRouteDistance = (s1, s2) -> {
            double shortestRouteS1 = Double.MAX_VALUE;
            double shortestRouteS2 = Double.MIN_VALUE;
            try {
                shortestRouteS1 = graph.obtainShortestRouteBetween(s1.getPosition(), point).getTotalDistance();
                shortestRouteS2 = graph.obtainShortestRouteBetween(s2.getPosition(), point).getTotalDistance();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
            return Double.compare(shortestRouteS1, shortestRouteS2);
        };
        List<Station> recommendedStations = validStationsToReturnBikeByRealRouteDistance(point, stations)
        		.stream().sorted(byRealRouteDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point) ) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }

}