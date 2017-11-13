package com.urjc.iagroup.bikesurbanfloats.util;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

/**
 * This class represents a circle which is used to delimit geographic areas.
 * @author IAgroup
 *
 */
public class BoundingCircle {
	/*
	 * This is the geographic point which represetns the center of the circle.
	 */
	private GeoPoint center;
	
	/**
	 * This is the radius of the circle in meters.
	 */
	private double radius; 
	
	public BoundingCircle(GeoPoint position, double radio) {
		this.center = position;
		this.radius = radio;
	}

	public GeoPoint getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}
	
	/**
	 * It calculates a random point inside the geographic area delimited by boundingCircle object. 
	 * @param random It is the general random instance of the system.
	 * @return a random point which belongs to thhe boundingCircle object. 
	 */
	public GeoPoint randomPointInCircle(SimulationRandom random) {
		double distance = Math.pow(random.nextDouble(), 0.5) * radius;
		double latitudeRadians = center.getLatitude() * GeoPoint.DEGREES_TO_RADIANS;
		double longitudeRadians = center.getLongitude() * GeoPoint.DEGREES_TO_RADIANS;
		double senLatitude = Math.sin(latitudeRadians);
		double cosLatitude = Math.cos(latitudeRadians);
		
		double bearing = random.nextDouble() * 2 * Math.PI;
		double theta = distance / GeoPoint.EARTH_RADIUS;
		double senBearing = Math.sin(bearing);
		double cosBearing = Math.cos(bearing);
		double senTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		
		double resLatRadians = Math.asin(senLatitude*cosTheta+cosLatitude*senTheta*cosBearing);
		double resLonRadians = longitudeRadians + Math.atan2(senBearing*senTheta*cosLatitude, 
				cosTheta-senLatitude*Math.sin(resLatRadians));
		resLonRadians = ((resLonRadians+(Math.PI*3))%(Math.PI*2))-Math.PI;
		
		double resLatitude = resLatRadians / GeoPoint.DEGREES_TO_RADIANS;
		double resLongitude = resLonRadians / GeoPoint.DEGREES_TO_RADIANS;

		return new GeoPoint(resLatitude, resLongitude);
	}
	
	public boolean isPointInCircle(GeoPoint pos) {
		return radius > center.distanceTo(pos);
	}

}
