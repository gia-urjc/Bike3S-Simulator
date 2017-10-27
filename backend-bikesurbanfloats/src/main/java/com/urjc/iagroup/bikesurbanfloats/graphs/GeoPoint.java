package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.math.BigDecimal;

public class GeoPoint {

    /**
     * Earth radius in meters
     */
    public final static double EARTH_RADIUS = 6371e3;
    public final static double DEG_TO_RAD = Math.PI / 180.0;

    private Double latitude;
    private Double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoPoint(GeoPoint geoPoint) {
        this(geoPoint.latitude, geoPoint.longitude);
    }

    public GeoPoint() {
        this(0.0, 0.0);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    /**
     * Calculates the distance to another point using the haversine formula.
     * @return The distance in meters.
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula on Wikipedia</a>
     */
    public double distanceTo(GeoPoint point) {
        double[] f = {Math.toRadians(this.latitude), Math.toRadians(point.latitude)};
        double[] l = {Math.toRadians(this.longitude), Math.toRadians(point.longitude)};
        double h = haversine(f[1] - f[0]) + Math.cos(f[0]) * Math.cos(f[1]) * haversine(l[1] - l[0]);
        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
    }
    
    private double haversine(double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }
    
    public double bearing(GeoPoint point2) {
		double latPos1Rad = Math.toRadians(latitude);
		double lonPos1Rad = Math.toRadians(longitude);
		double latPos2Rad = Math.toRadians(point2.getLatitude());
		double lonPos2Rad = Math.toRadians(point2.getLongitude());
		
		double y = Math.sin(lonPos2Rad - lonPos1Rad) * Math.cos(latPos2Rad);
		double x = Math.cos(latPos1Rad)*Math.sin(latPos2Rad) 
				- Math.sin(latPos1Rad)*Math.cos(latPos2Rad)*Math.cos(lonPos2Rad - lonPos1Rad);
		
		double bearing = Math.toDegrees(Math.atan2(y, x));
		
		return bearing;
    }
    
    public GeoPoint reachedPoint(double distance, GeoPoint destination) {
    	double latPosRad = Math.toRadians(latitude);
		double lonPosRad = Math.toRadians(longitude);
		double senLatitude = Math.sin(latPosRad);
		double cosLatitude = Math.cos(latPosRad);
		double bearing = Math.toRadians(this.bearing(destination));
		double theta = distance / GeoPoint.EARTH_RADIUS;
		double senBearing = Math.sin(bearing);
		double cosBearing = Math.cos(bearing);
		double senTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		
		double resLatRadians = Math.asin(senLatitude*cosTheta+cosLatitude*senTheta*cosBearing);
		double resLonRadians = lonPosRad + Math.atan2(senBearing*senTheta*cosLatitude, 
				cosTheta-senLatitude*Math.sin(resLatRadians));
		resLonRadians = ((resLonRadians+(Math.PI*3))%(Math.PI*2))-Math.PI;
    	
    	return new GeoPoint(Math.toDegrees(resLatRadians), Math.toDegrees(resLonRadians)); 
    }

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPoint geoPoint = (GeoPoint) o;

        return (geoPoint.latitude == this.latitude && geoPoint.longitude == this.longitude);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
    	String result = "Latitude: " + Double.parseDouble(String.format("%.6f", latitude));
    	result += "| Longitude: " + Double.parseDouble(String.format("%.6f", longitude)) + " \n";
    	return result;
    }
}
