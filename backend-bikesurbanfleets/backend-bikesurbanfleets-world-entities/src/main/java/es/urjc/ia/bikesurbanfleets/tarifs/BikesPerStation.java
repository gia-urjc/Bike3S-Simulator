package es.urjc.ia.bikesurbanfleets.tarifs;

import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

public class BikesPerStation {
	private Station station;
	private int bikes;
	public BikesPerStation(Station station, int bikes) {
		super();
		this.station = station;
		this.bikes = bikes;
	}
	
	public Station getStation() {
		return station;
	}
	public int getBikes() {
		return bikes;
	}
	
}
