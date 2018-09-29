package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class Recommendation {
	private Station station;   // recommended station
	private double incentive;   // discount
	public Recommendation(Station station, double incentive) {
		super();
		this.station = station;
		this.incentive = incentive;
	}
	public Station getStation() {
		return station;
	}
	public double getIncentive() {
		return incentive;
	}

	
}
