package es.urjc.ia.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Incentives.Incentive;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

public class Recommendation {

    private Station station;   // recommended station
    private Incentive incentive;   // discount
    private double probability;
    private double walkdistance;
    private double bikedistance;

    public double getWalkdistance() {
        return walkdistance;
    }

     public double getBikedistance() {
        return bikedistance;
    }

    public double getProbability() {
        return probability;
    }

    public Recommendation(Station station, double walkdist, double bikedist, double probability, Incentive incentive) {
        super();
        this.walkdistance=walkdist;
        this.bikedistance=bikedist;
        this.station = station;
        this.probability=probability;
        this.incentive = incentive;
    }

    public Station getStation() {
        return station;
    }

    public Incentive getIncentive() {
        return incentive;
    }

}
