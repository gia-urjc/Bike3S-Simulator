package es.urjc.ia.bikesurbanfleets.infraestructure.entities;

public class Order {
	private Station station;   // origin or destination station
	private int bikes;   // number of bikes to remove from a station or to bring to a station
	private String type;   // REMOVE or BRING bikes
	
	public Order(Station station, int bikes, String type) {
		super();
		this.station = station;
		this.bikes = bikes;
		this.type = type;
	}

	public Station getStation() {
		return station;
	}

	public int getBikes() {
		return bikes;
	}

	public String getType() {
		return type;
	}
	

}
