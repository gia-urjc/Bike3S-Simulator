package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.ArrayList;
import java.util.List;

public class GeoRoute {
	
	private List<GeoPoint> geoPointList;
	private double distance;
	private List<Double> distancesBetweenPoints;
	
	public GeoRoute(List<GeoPoint> geoPointList) throws IllegalStateException {
		if(geoPointList.size() < 2) {
			throw new IllegalStateException("Routes should have more than two points");
		}
		this.geoPointList = geoPointList;
		this.distancesBetweenPoints = new ArrayList<>();
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
	
	public GeoRoute calculateRouteByTimeAndVelocity(double finalTime, double velocity) throws Exception {
		double totalDistance = 0.0;
		double currentTime = 0.0;
		double currentDistance = 0.0;
		GeoPoint currentPoint = null;
		GeoPoint nextPoint = null;
		List<GeoPoint> newGeoPointList = new ArrayList<>();
		int i = 0;
		while(i < geoPointList.size()-1 && currentTime < finalTime) {
			currentPoint = geoPointList.get(i);
			nextPoint = geoPointList.get(i+1);
			currentDistance = distancesBetweenPoints.get(i);
			totalDistance += currentDistance;
			currentTime += currentDistance/velocity;	
			newGeoPointList.add(geoPointList.get(i));
			i++;
		}
		if(currentTime < finalTime) {
			throw new Exception("Can't create soubroute");
		}
		double x = totalDistance - finalTime*velocity;
		double intermedDistance = currentDistance - x;
		GeoPoint newPoint = currentPoint.reachedPoint(intermedDistance, nextPoint);
		newGeoPointList.add(newPoint);
		GeoRoute newRoute = new GeoRoute(newGeoPointList);
		return newRoute;	
	}
	
	@Override
	public String toString() {
		String result = "Points: \n";
		for(GeoPoint p: geoPointList) {
			result += p.getLatitude() + "," + p.getLongitude() + "\n";
		}
		result += "Distance: " + distance + " meters \n";
		result += "Distances between points: ";
		for(Double d: distancesBetweenPoints) {
			result += d + "\n";
		}
		return result;
	}
	
	
	

}
