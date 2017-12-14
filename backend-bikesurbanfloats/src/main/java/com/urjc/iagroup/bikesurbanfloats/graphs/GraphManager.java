package com.urjc.iagroup.bikesurbanfloats.graphs;

import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GraphHopperIntegrationException;

import java.util.List;

/**
 * This interface provides methods to manage the routes of a geographic map.
 * @author IAgroup
 *
 */
public interface GraphManager {
 /**
  * It calculates the possible routes between two geographic points.
  * @param startPosition It is the orign point.
  * @param endPosition It is the destination point.
  */
	void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GeoRouteCreationException, GraphHopperIntegrationException;
	
	/**
	 * It calculates which is the shortest route. 
	 * @return the shortest route of all posible routes between 2 points.
	 */
	GeoRoute getBestRoute() throws GraphHopperIntegrationException, GeoRouteCreationException;
	
	List<GeoRoute> getAllRoutes() throws GraphHopperIntegrationException, GeoRouteCreationException;

	List<GeoRoute> obtainAllRoutesBetween(GeoPoint originPoint, GeoPoint destinationPoint) throws GeoRouteCreationException, GraphHopperIntegrationException;
	
	GeoRoute obtainShortestRoute(GeoPoint originPoint, GeoPoint destinationPoint);
	/**
	 * It indicates if there are more than one possible route between two points.
	 * @return true if there're several possible routes between 2 points or false in other case. 
	 */
	boolean hasAlternativesRoute() throws GraphHopperIntegrationException;
}
