package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.Route;

public class GraphHopperImpl implements GraphManager {

	private GraphHopper hopper;
	private String locale;
	private GHResponse rsp;
	
	public GraphHopperImpl(String mapDir, String graphhopperDir, String encodingManager, String locale) throws IOException {
		FileUtils.deleteDirectory(new File(graphhopperDir));
		this.hopper = new GraphHopperOSM().forServer();
		this.locale = locale;
		hopper.setDataReaderFile(mapDir);
		hopper.setGraphHopperLocation(graphhopperDir);
		hopper.setEncodingManager(new EncodingManager(encodingManager));
		hopper.importOrLoad();
		
	}
	
	private Route responseGHToRoute(PathWrapper path) throws IllegalStateException {
		List<GeoPoint> geoPointList = new ArrayList<>();
    	PointList ghPointList = path.getPoints();
    	for(GHPoint3D p: ghPointList) {
    		geoPointList.add(new GeoPoint(p.getLat(), p.getLon()));
    	}
    	Route route = new Route(geoPointList);
    	return route;
	}
	
	public void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws Exception {
		GHRequest req = new GHRequest(
				startPosition.getLatitude(), startPosition.getLongitude(),
				endPosition.getLatitude(), endPosition.getLongitude()).
			setWeighting("fastest").
    	    setVehicle("foot").
    	    setLocale(Locale.forLanguageTag(locale));
    	GHResponse rsp = hopper.route(req);
    	
    	if(rsp.hasErrors()) {
     	   for(Throwable exception: rsp.getErrors()) {
     		  throw new Exception(exception.getMessage());
     	   }
     	}
    	this.rsp = rsp;
	}
	
	@Override
	public Route getBestRoute() throws Exception {
		if(rsp == null){
			throw new Exception("Route is not calculated");
		}
    	PathWrapper path = rsp.getBest();
    	return responseGHToRoute(path);
	}

	@Override
	public List<Route> getAllRoutes() throws Exception {
		if(rsp == null){
			throw new Exception("Route is not calculated");
		}
		List<Route> routes = new ArrayList<>();
		for(PathWrapper p: rsp.getAll()) {
			routes.add(responseGHToRoute(p));
		}
		return routes;
	}

	@Override
	public boolean hasAlternativesPath() throws Exception {
		if(rsp == null){
			throw new Exception("Route is not calculated");
		}
		return rsp.hasAlternatives();
	}
	

}
