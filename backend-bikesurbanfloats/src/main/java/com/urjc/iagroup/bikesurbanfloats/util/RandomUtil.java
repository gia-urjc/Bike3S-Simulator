package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

public class RandomUtil {
	
	private Random random;
	
	public RandomUtil(long seed) {
		this.random = new Random(seed);
	}
	
	public int nextInt(int min, int max) {	
		return min + random.nextInt((max - min) + 1);
	}
	
	public double nextDouble(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}

}
