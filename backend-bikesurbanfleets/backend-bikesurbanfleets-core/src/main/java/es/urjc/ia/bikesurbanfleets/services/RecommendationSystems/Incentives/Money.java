package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Incentives;

public class Money implements Incentive {
	private int cents;
	
	public Money(int cents) {
		this.cents = cents;
	}
	
	public int getValue() {
		return cents;
	}

}
