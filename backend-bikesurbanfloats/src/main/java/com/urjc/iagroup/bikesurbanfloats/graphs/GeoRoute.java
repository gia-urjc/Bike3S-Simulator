package com.urjc.iagroup.bikesurbanfloats.graphs;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;

import java.util.ArrayList;
import java.util.List;

public class GeoRoute {
	
	@Expose
	private List<GeoPoint> points;
	
	@Expose
	private double totalDistance;
	
	@Expose
	private List<Double> intermediateDistances;
	
	public GeoRoute(List<GeoPoint> geoPointList) throws GeoRouteCreationException {
		if(geoPointList.size() < 2) {
			throw new GeoRouteCreationException("Routes should have more than two points");
		}
		this.points = geoPointList;
		this.intermediateDistances = new ArrayList<>();
		calculateDistances();
	}
	
	public List<GeoPoint> getPoints() {
		return points;
	}
	
	public double getTotalDistance() {
		return totalDistance;
	}

	
	private void calculateDistances() {
		Double totalDistance = 0.0;
		for(int i = 0; i < points.size()-1; i++) {
			GeoPoint currentPoint = points.get(i);
			GeoPoint nextPoint = points.get(i+1);
			Double currentDistance = currentPoint.distanceTo(nextPoint);
			intermediateDistances.add(currentDistance);
			totalDistance += currentDistance;
		}
		this.totalDistance = totalDistance;
	}

	public GeoRoute calculateRouteByTimeAndVelocity(double finalTime, double velocity) throws GeoRouteException, GeoRouteCreationException {
		double totalDistance = 0.0;
		double currentTime = 0.0;
		double currentDistance = 0.0;
		GeoPoint currentPoint = null;
		GeoPoint nextPoint = null;
		List<GeoPoint> newGeoPointList = new ArrayList<>();
		int i = 0;
		while(i < points.size()-1 && currentTime < finalTime) {
			currentPoint = points.get(i);
			nextPoint = points.get(i+1);
			currentDistance = intermediateDistances.get(i);
			totalDistance += currentDistance;
			currentTime += currentDistance/velocity;	
			newGeoPointList.add(points.get(i));
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
		if (Double.doubleToLongBits(totalDistance) != Double.doubleToLongBits(other.totalDistance))
			return false;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String result = "Points: \n";
		for(GeoPoint p: points) {
			result += p.getLatitude() + "," + p.getLongitude() + "\n";
		}
		result += "Distance: " + totalDistance + " meters \n";
		result += "Distances between points: ";
		for(Double d: intermediateDistances) {
			result += d + "\n";
		}
		return result;
	}
	
	
	

}
