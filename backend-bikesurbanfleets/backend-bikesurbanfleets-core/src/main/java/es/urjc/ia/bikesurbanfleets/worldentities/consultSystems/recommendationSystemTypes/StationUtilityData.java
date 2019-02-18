package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

public class StationUtilityData {

    private Station station;
    private double Utility;
    private double distance;
    private double minoptimalocupation;
    private double maxopimalocupation;
    private double optimalocupation;
    private double capacity;
    private double ocupation;

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getOcupation() {
        return ocupation;
    }

    public void setOcupation(double ocupation) {
        this.ocupation = ocupation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public StationUtilityData(Station station) {
        super();
        this.station = station;
    }

    public void setUtility(double Utility) {
        this.Utility = Utility;
    }

    public double getUtility() {
        return Utility;
    }

    public Station getStation() {
        return station;
    }

    public double getMinoptimalocupation() {
        return minoptimalocupation;
    }

    public void setMinoptimalocupation(double minoptimalocupation) {
        this.minoptimalocupation = minoptimalocupation;
    }

    public double getMaxopimalocupation() {
        return maxopimalocupation;
    }

    public void setMaxopimalocupation(double maxopimalocupation) {
        this.maxopimalocupation = maxopimalocupation;
    }

    public double getOptimalocupation() {
        return optimalocupation;
    }

    public void setOptimalocupation(double optimalocupation) {
        this.optimalocupation = optimalocupation;
    }

}
