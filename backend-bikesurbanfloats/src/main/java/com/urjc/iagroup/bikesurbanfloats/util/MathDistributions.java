package com.urjc.iagroup.bikesurbanfloats.util;

public class MathDistributions {
	
	public static double poissonRandomInterarrivalDelay(double lambda) {
	    return (Math.log(1.0-Math.random())/-lambda);
	}

}
