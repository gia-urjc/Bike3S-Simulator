package com.urjc.iagroup.bikesurbanfloats.config.distributions;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

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
		RandomUtil random = new RandomUtil();
	    double randomValue = Math.log(1.0 - random.nextDouble(Double.MIN_VALUE, 1));
	    Double result = (double) -randomValue/lambda;
	    Long longResult = Math.round(result);
	    return longResult.intValue();
	}
	
}