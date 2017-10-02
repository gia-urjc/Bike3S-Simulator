package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class Distribution {
	private DistributionType distribution;

	public Distribution(DistributionType distribution) {
		this.distribution = distribution;
	}

	public DistributionType getDistribution() {
		return distribution;
	}

	public void setDistribution(DistributionType distribution) {
		this.distribution = distribution;
	}

}