package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class Recommendation {
	private Station station;
	private Incentive incentive;
	
	public Recommendation(Station station, Incentive incentive) {
		super();
		this.station = station;
		this.incentive = incentive;
	}

	public Station getStation() {
		return station;
	}

	public Incentive getIncentive() {
		return incentive;
	}
	
	

}
