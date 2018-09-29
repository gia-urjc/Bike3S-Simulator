package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class StationQuality {
	private Station station;
	private double quality;
	public StationQuality(Station station, double quality) {
		super();
		this.station = station;
		this.quality = quality;
	}
	public Station getStation() {
		return station;
	}
	public double getQuality() {
		return quality;
	}

}
