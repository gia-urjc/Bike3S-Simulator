package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class StationUtilityData {
	private Station station;
	private double currentUtility;
        private double distance;
        private double utilityincrement;
        
 
        
        
	public StationUtilityData(Station station, double quality, GeoPoint origin) {
		super();
		this.station = station;
		this.currentUtility = quality;
                this.distance=station.getPosition().distanceTo(origin);
	}
        public void setUtilityIncrement(double ui){
            utilityincrement=ui;
        }
        public double getUtilityIncrement(){
            return utilityincrement;
        }
	public Station getStation() {
		return station;
	}
	public double getCurrentUtility() {
		return currentUtility;
	}
	public double getDistance() {
		return distance;
	}

}
