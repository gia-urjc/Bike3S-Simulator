package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

public class StationUtilityData {
	private Station station;
	private double Utility; 
        private double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
        
	public StationUtilityData(Station station, double Utility) {
		super();
		this.station = station;
		this.Utility = Utility;
	}
        public double getUtility(){
            return Utility;
        }
	public Station getStation() {
		return station;
	}
}
