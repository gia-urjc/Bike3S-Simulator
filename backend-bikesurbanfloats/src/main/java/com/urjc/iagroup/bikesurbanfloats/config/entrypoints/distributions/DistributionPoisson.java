package com.urjc.iagroup.bikesurbanfloats.config.entrypoints.distributions;


import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

public class DistributionPoisson extends Distribution {
	private double lambda;
	
	public DistributionPoisson(DistributionType distribution, double lambda) {
		super(distribution);
		this.lambda = lambda;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
	
	
	/**
     * Calculates an exponential instant given a lambda parameter
     * @return A realistic exponential value given a lambda parameter
     * @see <a href="https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates">Generating exponential variates</a>
     */
	public int randomInterarrivalDelay() {
		StaticRandom random = StaticRandom.createRandom();
	    double randomValue = Math.log(1.0 - random.nextDouble(Double.MIN_VALUE, 1));
	    Double result = (double) -randomValue/lambda;
	    Long longResult = Math.round(result);
	    return longResult.intValue();
	}
	
}