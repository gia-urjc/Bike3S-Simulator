package com.urjc.iagroup.bikesurbanfloats.util;

public class MathDistributions {
	
	public static int poissonRandomInterarrivalDelay(double lambda) {
		
		double l = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
			k++;
			p *= Math.random();
		} while (p > l);
		
		return k-1;
	}

}
