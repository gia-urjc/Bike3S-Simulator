package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.concurrent.ThreadLocalRandom;

public class MathDistributions {
	
	/**
     * Calculates an exponential instant given a lambda parameter
     * @return A realistic exponential value given a lambda parameter
     * @see <a href="https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates">Generating exponential variates</a>
     */
	public static int poissonRandomInterarrivalDelay(double lambda) {
	    
	    double randomValue = Math.log(1.0 - ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, 1));
	    Double result = (double) -randomValue/lambda;
	    Long longResult = Math.round(result);
	    return longResult.intValue();
	
	}

}
