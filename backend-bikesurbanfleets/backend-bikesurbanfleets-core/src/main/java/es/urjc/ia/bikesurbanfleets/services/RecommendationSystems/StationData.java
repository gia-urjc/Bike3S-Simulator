/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems;

import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

/**
 *
 * @author holger
 */
public class StationData {

    private StationData(){};
    //this is for renting a bike
    public StationData(Station s, double distance, double time) {
        station = s;
        availableBikes = s.availableBikes();
        capacity=s.getCapacity();
        walkdist = distance;
        walktime = time;
    }
    
    //this is for returning a bike
    public StationData(Station s, double bikedistance, double biketime, 
            double walkdistance, double walktime) {
        station = s;
        capacity=s.getCapacity();
        availableSlots = s.availableSlots();
        this.bikedist = bikedistance;
        this.biketime = biketime;
        this.walkdist = walkdistance;
        this.walktime = walktime;
    }

    //basic data that is first calculated
    public Station station;
    public double availableBikes;
    public double availableSlots;
    public double capacity;
    
    //distances and time    
    public double bikedist; // used in return: dist and time with bike from currentposition to station
    public double biketime; // for returning bike
    public double walkdist; // used in return: dist and time for walikng from station to final destination
    public double walktime; // after returning bike
                            // used in take: dist and time for walikng from current position to station
                            // for getting a bike

    
    ///////
    //extra data that may be used in the calculation of different recommenders
    ///////
    
    // for recommenders that use expeted bikes /slots at arrivaltime
    public double expectedbikesAtArrival;
    public double expectedslotsAtArrival;

    
    //for local and global utility calculation
    public double Utility;
    public double minoptimalocupation;
    public double maxopimalocupation;
    public double optimalocupation;

    //for surrounding recommenders
    public    double quality ;
    public    Station nearest;
    public    double nearestDistance;
    public double surroundingAvBikes;
    public double surroundingCapacity;
  
    //info on best neighbour
    public Station bestNeighbour;
    public double bestNeighbourReturnWalktime;
    public double bestNeighbourProbability;

    //used in cost recommenders
    //cost analysis
    public double totalCost;
    public double individualCost;
    public double takecostdiff;
    public double returncostdiff;
    public double abandonProbability;
    public double expectedTimeIfNotAbandon;
    public double expectedUnsucesses;

    //probabilities
    public double probabilityTake;
    public double probabilityReturn;
    public double probabilityTakeAfterTake;
    public double probabilityReturnAfterTake;
    public double probabilityTakeAfterRerturn;
    public double probabilityReturnAfterReturn;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return false;
    }

}
