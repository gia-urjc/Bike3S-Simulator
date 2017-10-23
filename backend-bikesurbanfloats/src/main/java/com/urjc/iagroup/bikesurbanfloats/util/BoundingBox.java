package com.urjc.iagroup.bikesurbanfloats.util;

public class BoundingBox {
	
	private GeoPoint northWest;
	private GeoPoint southEast;

	
	public BoundingBox(GeoPoint point1, GeoPoint point2) {
		this.northWest = point1;
		this.southEast = point2;
	}
	
	public BoundingBox(double lat1, double lon1, double lat2, double lon2) {
		this.northWest = new GeoPoint(lat1, lon1);
		this.southEast = new GeoPoint(lat2, lon2);
	}
	
	public GeoPoint getPoint1() {
		return northWest;
	}
	
	public GeoPoint getPoint2() {
		return southEast;
	}
	
	public double getWidth() {
		GeoPoint auxGeoPoint = new GeoPoint(northWest.getLatitude(), southEast.getLongitude());
		return northWest.distanceTo(auxGeoPoint);
	}
	
	public double getHeight() {
		GeoPoint auxGeoPoint = new GeoPoint(southEast.getLatitude(), northWest.getLongitude());
		return northWest.distanceTo(auxGeoPoint);
	}
	 
	public GeoPoint randomPoint() {
		double newLatitude = StaticRandom.nextDouble(northWest.getLatitude(), southEast.getLatitude());
		double newLongitude = StaticRandom.nextDouble(northWest.getLongitude(), southEast.getLongitude());
		return new GeoPoint(newLatitude, newLongitude);
	}

}
