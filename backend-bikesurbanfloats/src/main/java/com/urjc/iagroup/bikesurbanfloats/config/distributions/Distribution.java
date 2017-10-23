package com.urjc.iagroup.bikesurbanfloats.config.distributions;

import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class Distribution {
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