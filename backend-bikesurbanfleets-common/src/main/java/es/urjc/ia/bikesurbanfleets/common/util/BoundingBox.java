package es.urjc.ia.bikesurbanfleets.common.util;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

/**
 * This class represents a rectangle which is used to delimit geographic areas.
 * @author IAgroup
 *
 */
public class BoundingBox {
    /**
     * It is upper left corner of the rectangle.
    */
    private GeoPoint northWest;

    /**
     * It is the lower right corner of the rectangle.
     */
    private GeoPoint southEast;

    
    public BoundingBox(GeoPoint northWest, GeoPoint southEast) {
        this.northWest = northWest;
        this.southEast = southEast;
    }
    
    public BoundingBox(double north, double west, double south, double east) {
        this.northWest = new GeoPoint(north, west);
        this.southEast = new GeoPoint(south, east);
    }
    
    public GeoPoint getNorthWest() {
        return northWest;
    }
    
    public GeoPoint getSouthEast() {
        return southEast;
    }
    
    public double getWidth() {
        GeoPoint northEast = new GeoPoint(northWest.getLatitude(), southEast.getLongitude());
        return northWest.distanceTo(northEast);
    }
    
    public double getHeight() {
        GeoPoint southWest = new GeoPoint(southEast.getLatitude(), northWest.getLongitude());
        return northWest.distanceTo(southWest);
    }
    
    /**
     * It calculates a random point inside the geographic area delimited by boundingBox object. 
     * @param random It is the general random instance of the system. 
     * @return a random point which belongs to thhe boundingBox object. 
     */
    public GeoPoint randomPoint(SimulationRandomm random) {
        double newLatitude = random.nextDouble(northWest.getLatitude(), southEast.getLatitude());
        double newLongitude = random.nextDouble(northWest.getLongitude(), southEast.getLongitude());
        return new GeoPoint(newLatitude, newLongitude);
    }

}
