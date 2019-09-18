package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;
import java.util.Objects;

public class StationUtilityData {

    private double Utility;
   
    
    //station information
    private Station station;
    private double minoptimalocupation;
    private double maxopimalocupation;
    private double optimalocupation;
    private double capacity;
    private double availableBikes;
    
    //info on best neighbour
    public StationUtilityData bestNeighbour;
    
    //distances and time 
    private double bikedist;
    private double walkdist;
    private double walktime;
    private double biketime;
    
    //cost analysis
    private double totalCost;
    private double individualCost;
    private double takecostdiff;
    private double returncostdiff;
    
    //probabilities
        private double probabilityTake;
        private double probabilityReturn;
        private double probabilityTakeAfterTake;
        private double probabilityReturnAfterTake;
        private double probabilityTakeAfterRerturn;
        private double probabilityReturnAfterReturn;
        
    public double getProbabilityTakeAfterTake() {
        return probabilityTakeAfterTake;
    }

    public StationUtilityData setProbabilityTakeAfterTake(double probabilityTakeAfterTake) {
        this.probabilityTakeAfterTake = probabilityTakeAfterTake;
        return this;
    }

    public double getProbabilityReturnAfterTake() {
        return probabilityReturnAfterTake;
    }

    public StationUtilityData setProbabilityReturnAfterTake(double probabilityReturnAfterTake) {
        this.probabilityReturnAfterTake = probabilityReturnAfterTake;
        return this;
    }

    public double getProbabilityTakeAfterRerturn() {
        return probabilityTakeAfterRerturn;
    }

    public StationUtilityData setProbabilityTakeAfterRerturn(double probabilityTakeAfterRerturn) {
        this.probabilityTakeAfterRerturn = probabilityTakeAfterRerturn;
        return this;
    }

    public double getProbabilityReturnAfterReturn() {
        return probabilityReturnAfterReturn;
    }

    public StationUtilityData setProbabilityReturnAfterReturn(double probabilityReturnAfterReturn) {
        this.probabilityReturnAfterReturn = probabilityReturnAfterReturn;
        return this;
    }


    public double getTotalCost() {
        return totalCost;
    }

    public double getBikedist() {
        return bikedist;
    }

    public StationUtilityData setBikedist(double bikedist) {
        this.bikedist = bikedist;
        return this;
    }

    public double getWalkdist() {
        return walkdist;
    }

    public StationUtilityData setWalkdist(double walkdist) {
        this.walkdist = walkdist;
        return this;
    }

    public StationUtilityData setTotalCost(double totalCost) {
        this.totalCost = totalCost;
        return this;           
    }

    public double getTakecostdiff() {
        return takecostdiff;
    }

    public StationUtilityData setTakecostdiff(double takecostdiff) {
        this.takecostdiff = takecostdiff;
        return this;           
    }

    public double getReturncostdiff() {
        return returncostdiff;
    }

    public StationUtilityData setReturncostdiff(double returncostdiff) {
        this.returncostdiff = returncostdiff;
        return this;           
    }

    public double getProbabilityTake() {
        return probabilityTake;
    }

    public StationUtilityData setProbabilityTake(double probabilityTake) {
        this.probabilityTake = probabilityTake;
        return this;           
    }

    public double getProbabilityReturn() {
        return probabilityReturn;
    }

    public StationUtilityData setProbabilityReturn(double probabilityReturn) {
        this.probabilityReturn = probabilityReturn;
        return this;           
    }


    public double getIndividualCost() {
        return individualCost;
    }

    public StationUtilityData setIndividualCost(double Cost) {
        this.individualCost = Cost;
        return this;           
    }

    public double getCapacity() {
        return capacity;
    }

    public StationUtilityData setCapacity(double capacity) {
        this.capacity = capacity;
        return this;
    }

    public double getAvailableBikes() {
        return availableBikes;
    }

    public StationUtilityData setAvailableBikes(double ocupation) {
        this.availableBikes = ocupation;
        return this;
    }

    public double getWalkTime() {
        return walktime;
    }

    public StationUtilityData setWalkTime(double time) {
        this.walktime = time;
        return this;
    }

    public double getBiketime() {
        return biketime;
    }

    public StationUtilityData setBiketime(double biketime) {
        this.biketime = biketime;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return false;
    }

}
