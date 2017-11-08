package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;

public class GeoRoute {
	
	@Expose
	private List<GeoPoint> pointList;
	
	@Expose
	private double distance;
	
	@Expose
	private List<Double> distancesBetweenPointsList;
	
	public GeoRoute(List<GeoPoint> geoPointList) throws GeoRouteCreationException {
		if(geoPointList.size() < 2) {
			throw new GeoRouteCreationException("Routes should have more than two points");
		}
		this.pointList = geoPointList;
		this.distancesBetweenPointsList = new ArrayList<>();
		calculateDistances();
	}
	
	public List<GeoPoint> getPointList() {
		return pointList;
	}
	
	public double getDistance() {
		return distance;
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

	public GeoRoute calculateRouteByTimeAndVelocity(double finalTime, double velocity) throws GeoRouteException, GeoRouteCreationException {
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
			throw new GeoRouteException("Can't create soubroute");
		}
		double x = totalDistance - finalTime*velocity;
		double intermedDistance = currentDistance - x;
		GeoPoint newPoint = currentPoint.reachedPoint(intermedDistance, nextPoint);
		newGeoPointList.add(newPoint);
		GeoRoute newRoute = new GeoRoute(newGeoPointList);
		return newRoute;	
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeoRoute other = (GeoRoute) obj;
		if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
			return false;
		if (pointList == null) {
			if (other.pointList != null)
				return false;
		} else if (!pointList.equals(other.pointList))
			return false;
		return true;
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
