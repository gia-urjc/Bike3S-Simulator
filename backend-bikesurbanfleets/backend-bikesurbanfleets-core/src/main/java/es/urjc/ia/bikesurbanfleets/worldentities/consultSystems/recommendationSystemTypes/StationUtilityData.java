package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.Objects;

public class StationUtilityData {

    private Station station;
    private double Utility;
   
    private double time;
    private double minoptimalocupation;
    private double maxopimalocupation;
    private double optimalocupation;
    private double capacity;
    private double ocupation;
    private double probability;
    private double distance;
    public int closest;
    public double closestbikedist;
    public double closestprob;
    public double bikedist;
    public double walkdist;
    public double closestwalkdist;
    private double Cost;

    public double getCost() {
        return Cost;
    }

    public void setCost(double Cost) {
        this.Cost = Cost;
    }

    public double getDistance() {
        return distance;
    }

    public StationUtilityData setDistance(double distance) {
        this.distance = distance;
        return this;           
    }

    public double getCapacity() {
        return capacity;
    }

    public StationUtilityData setCapacity(double capacity) {
        this.capacity = capacity;
        return this;
    }

    public double getOcupation() {
        return ocupation;
    }

    public StationUtilityData setOcupation(double ocupation) {
        this.ocupation = ocupation;
        return this;
    }

    public double getTime() {
        return time;
    }

    public StationUtilityData setTime(double time) {
        this.time = time;
        return this;
    }

    public StationUtilityData(Station station) {
        super();
        this.station = station;
    }

    public StationUtilityData setUtility(double Utility) {
        this.Utility = Utility;
        return this;
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

    public StationUtilityData setMinoptimalocupation(double minoptimalocupation) {
        this.minoptimalocupation = minoptimalocupation;
        return this;
    }

    public double getMaxopimalocupation() {
        return maxopimalocupation;
    }

    public StationUtilityData setMaxopimalocupation(double maxopimalocupation) {
        this.maxopimalocupation = maxopimalocupation;
        return this;
    }

    public double getOptimalocupation() {
        return optimalocupation;
    }

    public StationUtilityData setOptimalocupation(double optimalocupation) {
        this.optimalocupation = optimalocupation;
        return this;
    }

    public double getProbability() {
        return probability;
    }

    public StationUtilityData setProbability(double probability) {
        this.probability = probability;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return false;
    }

}
