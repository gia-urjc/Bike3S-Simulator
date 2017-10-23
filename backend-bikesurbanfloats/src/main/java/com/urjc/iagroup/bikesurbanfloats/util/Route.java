package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.List;

public class Route {
	
	private List<GeoPoint> geoPointList;
	private double distance;
	private List<Double> distancesBetweenPoints;
	
	public Route(List<GeoPoint> geoPointList) {
		this.geoPointList = geoPointList;
	}
	
	private void calculateDistances() {
		Double totalDistance = 0.0;
		for(int i = 0; i < geoPointList.size()-1; i++) {
			GeoPoint currentGPoint = geoPointList.get(i);
			GeoPoint nextGPoint = geoPointList.get(i+1);
			Double currentDistance = currentGPoint.distanceTo(nextGPoint);
			distancesBetweenPoints.add(currentDistance);
			totalDistance += currentDistance;
		}
	}
	
	
	

}
