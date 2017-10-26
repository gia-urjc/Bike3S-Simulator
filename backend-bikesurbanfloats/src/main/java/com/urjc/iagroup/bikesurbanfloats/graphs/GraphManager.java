package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.Route;

public interface GraphManager {
	
	void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws Exception;
	
	Route getBestRoute() throws Exception;
	
	List<Route> getAllRoutes() throws Exception;
	
	boolean hasAlternativesPath() throws Exception;

	
}
