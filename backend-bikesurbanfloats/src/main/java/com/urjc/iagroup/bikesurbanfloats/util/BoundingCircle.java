package com.urjc.iagroup.bikesurbanfloats.util;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

public class BoundingCircle {
	
	private GeoPoint position;
	private double radio; //meters
	
	public BoundingCircle(GeoPoint position, double radio) {
		this.position = position;
		this.radio = radio;
	}

	public GeoPoint getPosition() {
		return position;
	}

	public double getRadio() {
		return radio;
	}
	
	/**
	 * Determine a random GeoPoint given a GeoPoint and a distance
	 * @param distance to point of the new random GeoPoint
	 * @return random GeoPoint
	 */
	private GeoPoint randomPointByDistance(double distance) {
		
		double latitudeRadians = position.getLatitude() * GeoPoint.DEG_TO_RAD;
		double longitudeRadians = position.getLongitude() * GeoPoint.DEG_TO_RAD;
		double senLatitude = Math.sin(latitudeRadians);
		double cosLatitude = Math.cos(latitudeRadians);
		
		double bearing = StaticRandom.nextDouble() * 2 * Math.PI;
		double theta = distance / GeoPoint.EARTH_RADIUS;
		double senBearing = Math.sin(bearing);
		double cosBearing = Math.cos(bearing);
		double senTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		
		double resLatRadians = Math.asin(senLatitude*cosTheta+cosLatitude*senTheta*cosBearing);
		double resLonRadians = longitudeRadians + Math.atan2(senBearing*senTheta*cosLatitude, 
				cosTheta-senLatitude*Math.sin(resLatRadians));
		resLonRadians = ((resLonRadians+(Math.PI*3))%(Math.PI*2))-Math.PI;
		
		double resLatitude = resLatRadians / GeoPoint.DEG_TO_RAD;
		double resLongitude = resLonRadians / GeoPoint.DEG_TO_RAD;
		
		return new GeoPoint(resLatitude, resLongitude);
	}

	public GeoPoint randomPointInCircle() {
		double randomDistance = Math.pow(StaticRandom.nextDouble(), 0.5) * radio;
		return randomPointByDistance(randomDistance);
	}
	
	public boolean isPointInCircle(GeoPoint pos) {
		return radio > position.distanceTo(pos);
	}

}
