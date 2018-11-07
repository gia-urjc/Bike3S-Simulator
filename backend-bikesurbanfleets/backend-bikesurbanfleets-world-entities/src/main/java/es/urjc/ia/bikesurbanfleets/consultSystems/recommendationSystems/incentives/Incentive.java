package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems.incentives;

public class Incentive<T> {
	private T value;
	
	public Incentive(T v) {
		this.value = v;
	}
	
	public T getValue() {
		return value;
	}
}
