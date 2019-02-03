package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class StationUtilityData {
	private Station station;
	private double Utility;        
        
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
