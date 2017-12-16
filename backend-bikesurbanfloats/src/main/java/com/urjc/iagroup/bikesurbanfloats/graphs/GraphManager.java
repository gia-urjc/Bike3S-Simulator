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
	

	List<GeoRoute> obtainAllRoutesBetween(GeoPoint originPoint, GeoPoint destinationPoint) throws GeoRouteCreationException, GraphHopperIntegrationException;
	
	/**
	 * It calculates which is the shortest route. 
	 * @return the shortest route of all posible routes between 2 points.
	 * @throws GraphHopperIntegrationException, GeoRouteCreationException 
	 */
	GeoRoute obtainShortestRouteBetween(GeoPoint originPoint, GeoPoint destinationPoint) throws GraphHopperIntegrationException, GraphHopperIntegrationException, GeoRouteCreationException;

	/**
	 * It indicates if there are more than one possible route between two points.
	 * @return true if there're several possible routes between 2 points or false in other case. 
	 * @throws GraphHopperIntegrationException
	 */
	boolean hasAlternativesRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperIntegrationException;
}
