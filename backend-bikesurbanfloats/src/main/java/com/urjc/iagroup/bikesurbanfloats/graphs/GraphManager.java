package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GraphHopperImplException;

public interface GraphManager {
	
	void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GeoRouteCreationException, GraphHopperImplException;
	
	GeoRoute getBestRoute() throws GraphHopperImplException, GeoRouteCreationException;
	
	List<GeoRoute> getAllRoutes() throws GraphHopperImplException, GeoRouteCreationException;
	
	boolean hasAlternativesPath() throws GraphHopperImplException;
	
}
