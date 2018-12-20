package es.urjc.ia.bikesurbanfleets.common.graphs;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a geographic route.
 *
 * @author IAgroup
 *
 */
public class GeoRoute {

    /**
     * These are the points which forms the route.
     */
    @Expose
    private String encodedPoints;

    /**
     * This is the distance of the entire route.
     */
    private double totalDistance;
    
    /**
     * It creates a route which has at least 2 points.
     *
     * @param geoPointList It is the list of points which form the route.
     */
    public GeoRoute(List<GeoPoint> geoPointList) throws GeoRouteCreationException {
         if (geoPointList.size() < 2) {
            throw new GeoRouteCreationException("Routes should have more than two points");
        } else {
            this.encodedPoints=encode(geoPointList);
            this.totalDistance=calculateDistance(encodedPoints);
        }
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * It calculates the distances of the different sections of the route and
     * its total distance.
     */
    private static double calculateDistance(String pointString) {
    	List<GeoPoint> points = decode(pointString);
        Double totalDistance = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            GeoPoint currentPoint = points.get(i);
            GeoPoint nextPoint = points.get(i + 1);
            Double currentDistance = currentPoint.distanceTo(nextPoint);
            totalDistance += currentDistance;
        }
        return totalDistance;
    }

        /**
     * Given the speed at which the route is traveled and the time during which
     * the entity has been traveling, it calculates the section of the route
     * that the entity has traveled.
     *
     * @param finalTime It is the time during which the entity has been
     * traveling.
     * @param velocity It is the speed at which the entity travels.
     * @return the traveled subroute.
     */
    public GeoPoint calculatePositionByTimeAndVelocity(double finalTime, double velocity) throws GeoRouteException, GeoRouteCreationException {
        //get the points as list
        List<GeoPoint> points = decode(encodedPoints);
        double totalDistance = 0.0;
        double currentTime = 0.0;   // time the user has been travelling through the route to the next inmediate known geographical point when reservation has expired
        double currentDistance = 0.0;  // distance of the current part of the route the user is travelling through (distance beteween echa pair of geographical points) 
        GeoPoint currentPoint = null;   //geographical point at which user is at this moment
        GeoPoint nextPoint = null;   // next geographical p o in t the user is going to reach
        int i = 0;
        
        while (i < points.size() - 1 && currentTime < finalTime) {
            currentPoint = points.get(i);
            nextPoint = points.get(i + 1);
            currentDistance = currentPoint.distanceTo(nextPoint);
            totalDistance += currentDistance;
            currentTime += currentDistance / velocity;
            i++;
        }
               
        /* If timeout has happened, it is because the user takes to arrive at 
         * his destination more time that reservation valid time. Then, if time that user 
         * takes to arrive is lower than reservation valid time, this is an error: timeout mustn't ocurrs 	
         */
        if (currentTime < finalTime) {
            throw new GeoRouteException("Can't create intermediate position");
        }
        
        GeoPoint newPoint = nextPoint; 
        if (currentTime  != finalTime) {
	        /* finalTime * velocity is the real distance the user has been able to travel until the timeout.
	         * x is the distance between th known geographical point the user has 
	         * reached and the real geographical point the user has arrived at.
	         */
	        double x = totalDistance - finalTime * velocity;
	        double intermedDistance = currentDistance - x;
	        newPoint = currentPoint.reachedPoint(intermedDistance, nextPoint);
        }
        return newPoint;
    }

    public GeoRoute concatRoute(GeoRoute route) throws GeoRouteCreationException {
        List<GeoPoint> points1 = decode(encodedPoints);
        List<GeoPoint> points2=decode(route.encodedPoints);
        List<GeoPoint> newPoints = new ArrayList<>();
        points1.stream().forEach(point -> newPoints.add(point));
        points2.stream().forEach(point -> newPoints.add(point));
        return new GeoRoute(newPoints);

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GeoRoute other = (GeoRoute) obj;
        if (Double.doubleToLongBits(totalDistance) != Double.doubleToLongBits(other.totalDistance)) {
            return false;
        }
        if (encodedPoints == null) {
            if (other.encodedPoints != null) {
                return false;
            }
        } else if (!encodedPoints.equals(other.encodedPoints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        List<GeoPoint> points=decode(encodedPoints);
        String result = "Points: \n";
        for (GeoPoint p : points) {
            result += p.getLatitude() + "," + p.getLongitude() + "\n";
        }
        result += "Distance: " + totalDistance + " meters \n";
        result += "Distances between points: ";
        return result;
    }

    // Decodes an encoded path string into a sequence of points as used by google
    public static List<GeoPoint> decode(final String encodedpoints) {

        int len = encodedpoints.length();
        List<GeoPoint> path = new ArrayList<>(len / 2);
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedpoints.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedpoints.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new GeoPoint(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    // Encodes a sequence of GeoPoints into a string, as used by goolge
    public static String encode(final List<GeoPoint> path) {
        long lastLat = 0;
        long lastLng = 0;

        StringBuffer result = new StringBuffer();

        for (final GeoPoint point : path) {
            long lat = Math.round(point.getLatitude() * 1e5);
            long lng = Math.round(point.getLongitude() * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }
    
    private static void encode(long v, StringBuffer result) {
    v = v < 0 ? ~(v << 1) : v << 1;
    while (v >= 0x20) {
      result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
      v >>= 5;
    }
    result.append(Character.toChars((int) (v + 63)));
  }
}
