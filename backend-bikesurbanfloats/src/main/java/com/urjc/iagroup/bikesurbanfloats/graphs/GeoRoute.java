package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.ArrayList;
import java.util.List;

public class GeoRoute {
	
	private List<GeoPoint> pointList;
	private double distance;
	private List<Double> distancesBetweenPointsList;
	
	public GeoRoute(List<GeoPoint> geoPointList) throws IllegalStateException {
		if(geoPointList.size() < 2) {
			throw new IllegalStateException("Routes should have more than two points");
		}
		this.pointList = geoPointList;
		this.distancesBetweenPointsList = new ArrayList<>();
		calculateDistances();
	}
	
	private void calculateDistances() {
		Double totalDistance = 0.0;
		for(int i = 0; i < pointList.size()-1; i++) {
			GeoPoint currentPoint = pointList.get(i);
			GeoPoint nextPoint = pointList.get(i+1);
			Double currentDistance = currentPoint.distanceTo(nextPoint);
			distancesBetweenPointsList.add(currentDistance);
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
		while(i < pointList.size()-1 && currentTime < finalTime) {
			currentPoint = pointList.get(i);
			nextPoint = pointList.get(i+1);
			currentDistance = distancesBetweenPointsList.get(i);
			totalDistance += currentDistance;
			currentTime += currentDistance/velocity;	
			newGeoPointList.add(pointList.get(i));
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
		for(GeoPoint p: pointList) {
			result += p.getLatitude() + "," + p.getLongitude() + "\n";
		}
		result += "Distance: " + distance + " meters \n";
		result += "Distances between points: ";
		for(Double d: distancesBetweenPointsList) {
			result += d + "\n";
		}
		return result;
	}
	
	
	

}
