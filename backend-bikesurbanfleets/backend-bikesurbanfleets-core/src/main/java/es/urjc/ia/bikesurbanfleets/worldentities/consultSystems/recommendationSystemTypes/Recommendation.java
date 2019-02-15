package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystems.incentives.Incentive;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

public class Recommendation {
	private Station station;   // recommended station
	private Incentive incentive;   // discount
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
