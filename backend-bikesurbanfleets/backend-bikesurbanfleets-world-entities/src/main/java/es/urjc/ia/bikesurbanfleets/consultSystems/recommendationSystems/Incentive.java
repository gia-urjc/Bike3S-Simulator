package es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystems;

public class Incentive<T extends Comparable<T>> implements Comparable<Incentive<T>> {
	
	private T value;
	
	public T getValue() {
		return value;
	}

	@Override
	public int compareTo(Incentive<T> incentive) {
		T value1 = this.value;
		T value2 = incentive.getValue();
		return value1.compareTo(value2);
	}
	
}
