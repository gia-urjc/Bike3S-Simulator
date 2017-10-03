package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class Distribution {
	private DistributionType distributionType;

	public Distribution(DistributionType distributionType) {
		this.distributionType = distributionType;
	}

	public DistributionType getDistribution() {
		return distributionType;
	}

	public void setDistribution(DistributionType distribution) {
		this.distributionType = distribution;
	}


}