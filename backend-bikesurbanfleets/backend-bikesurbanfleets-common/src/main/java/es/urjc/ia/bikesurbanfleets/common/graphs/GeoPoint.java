package es.urjc.ia.bikesurbanfleets.common.graphs;

import com.google.gson.annotations.Expose;

/**
 * This class represents a geographic point.
 * @author IAgroup
 *
 */
public class GeoPoint {

    /**
     * It is the earth radius in meters.
     */
    public final static double EARTH_RADIUS = 6371e3;

    /**
     * It represents how many radians is a degree.
     */
    public final static double DEGREES_TO_RADIANS = Math.PI / 180.0;

    @Expose
    private double latitude;

    @Expose
    private double longitude;

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
     * It calculates the distance to another point using the haversine formula.
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

    /**
     * It calculates the angle formed by two points with respect an axis.
     * @param point2 It is the second point which determines the angle.
     * @return an angle in radians.
     */
    public double bearing(GeoPoint point) {
        // the latitudes and longitudes of these points are in radians
        double latitudePoint1 = Math.toRadians(latitude);
        double longitudePoint1 = Math.toRadians(longitude);
        double latitudePoint2 = Math.toRadians(point.getLatitude());
        double longitudePoint2 = Math.toRadians(point.getLongitude());

        double y = Math.sin(longitudePoint2 - longitudePoint1) * Math.cos(latitudePoint2);
        double x = Math.cos(latitudePoint1)*Math.sin(latitudePoint2)
                - Math.sin(latitudePoint1)*Math.cos(latitudePoint2)*Math.cos(longitudePoint2 - longitudePoint1);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        return bearing;
    }

    /**
     * A........*----B
     * A is the origin point; B is the destination point; the line formed by dots is the ddistance parameter.
     * Given a distance, it calculates a point between two points.
     * @param distance It is the distance between the origin point and the reached point.
     * @param destination It is the destination point.
     * @return the reached point between the origin and the destination points.
     */
    public GeoPoint reachedPoint(double distance, GeoPoint destination) {
        // these 2 variables are in radians
        double latitudePoint = Math.toRadians(latitude);
        double longitudePoint = Math.toRadians(longitude);

        double senLatitude = Math.sin(latitudePoint);
        double cosLatitude = Math.cos(latitudePoint);
        double bearing = Math.toRadians(this.bearing(destination));
        double theta = distance / GeoPoint.EARTH_RADIUS;
        double senBearing = Math.sin(bearing);
        double cosBearing = Math.cos(bearing);
        double senTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        double resLatRadians = Math.asin(senLatitude*cosTheta+cosLatitude*senTheta*cosBearing);
        double resLonRadians = longitudePoint + Math.atan2(senBearing*senTheta*cosLatitude,
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
    public String toString() {
        String result = "Latitude: " + latitude;
        result += "| Longitude: " + longitude + " \n";
        return result;
    }
}
