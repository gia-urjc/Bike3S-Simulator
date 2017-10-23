package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.List;

public class Route {
	
	private List<GeoPoint> geoPointList;
	private double distance;
	private List<Double> distancesBetweenPoints;
	
	public Route(List<GeoPoint> geoPointList) {
		this.geoPointList = geoPointList;
		calculateDistances();
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
		distance = totalDistance;
	}
	
	public Route calculateSubRoute(double finalTime, double velocity) throws Exception {
		//t = d/v
		double currentDistance = 0.0;
		double currentTime = 0.0;
		List<Double> newGeoPointList;
		int i = 0;
		while(i < geoPointList.size()-1 && currentTime < finalTime) {
			currentDistance += distancesBetweenPoints.get(i);
			currentTime += currentDistance/velocity;
			i++;
		}
		if(currentTime < finalTime) {
			throw new Exception("Can't create soubroute");
		}
		//TODO calculate point
		
		return null;
		
	}
	
	
	

}
