package com.urjc.iagroup.bikesurbanfloats.util;

public class BoundingBox {
	
	private GeoPoint point1;
	private GeoPoint point2;

	
	public BoundingBox(GeoPoint point1, GeoPoint point2) {
		this.point1 = point1;
		this.point2 = point2;
	}
	
	public BoundingBox(double lat1, double lon1, double lat2, double lon2) {
		this.point1 = new GeoPoint(lat1, lon1);
		this.point2 = new GeoPoint(lat2, lon2);
	}
	
	public GeoPoint getPoint1() {
		return point1;
	}
	
	public GeoPoint getPoint2() {
		return point2;
	}
	
	public double getWidth() {
		GeoPoint auxGeoPoint = new GeoPoint(point1.getLatitude(), point2.getLongitude());
		return point1.distanceTo(auxGeoPoint);
	}
	
	public double getHeight() {
		GeoPoint auxGeoPoint = new GeoPoint(point2.getLatitude(), point1.getLongitude());
		return point1.distanceTo(auxGeoPoint);
	}
	 
	public GeoPoint randomPoint() {
		double newLatitude = StaticRandom.nextDouble(point1.getLatitude(), point2.getLatitude());
		double newLongitude = StaticRandom.nextDouble(point1.getLongitude(), point2.getLongitude());
		return new GeoPoint(newLatitude, newLongitude);
	}

}
