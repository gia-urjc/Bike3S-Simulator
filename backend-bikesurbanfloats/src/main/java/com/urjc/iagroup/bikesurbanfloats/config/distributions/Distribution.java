	package com.urjc.iagroup.bikesurbanfloats.config.distributions;

public class Distribution {
	
	public enum DistributionType {
		POISSON, RANDOM, NONEDISTRIBUTION
	}

	private DistributionType type;

	public Distribution(DistributionType type) {
		this.type = type;
	}

	public DistributionType getDistribution() {
		return type;
	}

	public void setDistribution(DistributionType distribution) {
		this.type = distribution;
	}


}