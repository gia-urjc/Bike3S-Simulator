package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.List;

public interface GraphManager {
	
	void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws Exception;
	
	GeoRoute getBestRoute() throws Exception;
	
	List<GeoRoute> getAllRoutes() throws Exception;
	
	boolean hasAlternativesPath() throws Exception;

	
}
