package com.urjc.iagroup.bikesurbanfloats.util;

public class GeoPoint {

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

    public Double distanceTo(GeoPoint point) {
        GeoPoint delta = new GeoPoint(point.latitude - this.latitude, point.longitude - this.longitude);
        return Math.sqrt(Math.pow(delta.latitude, 2) + Math.pow(delta.longitude, 2));
    }
}
