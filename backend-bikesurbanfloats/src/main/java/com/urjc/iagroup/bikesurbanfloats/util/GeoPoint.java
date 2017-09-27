package com.urjc.iagroup.bikesurbanfloats.util;

public class GeoPoint {

    /**
     * Earth radius in meters
     */
    private final static Double EARTH_RADIUS = 6371e3;

    private Double latitude;
    private Double longitude;

    public GeoPoint(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoPoint() {
        this(0.0, 0.0);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Calculates the distance to another point using the haversine formula.
     * @return The distance in meters.
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula on Wikipedia</a>
     */
    public Double distanceTo(GeoPoint point) {
        Double[] f = {Math.toRadians(this.latitude), Math.toRadians(point.latitude)};
        Double[] l = {Math.toRadians(this.longitude), Math.toRadians(point.longitude)};
        Double h = haversine(f[1] - f[0]) + Math.cos(f[0]) * Math.cos(f[1]) * haversine(l[1] - l[0]);
        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
    }

    private Double haversine(Double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }
    
    @Override
    public String toString() {
    	String result = "Latitude: " + Double.toString(latitude);
    	result += "| Longitude: " + Double.toString(longitude) + " \n";
    	return result;
    }
}
