package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class RandomUtil {
	
	private Random random;
	
	public RandomUtil(long seed) {
		this.random = new Random(seed);
	}
	
	public int nextInt(int min, int max) {	
		return min + random.nextInt((max - min) + 1);
	}
	
	public int nextInt() {
		return random.nextInt();
	}
	
	public double nextDouble(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}
	
	public double nextDouble() {
		return random.nextDouble();
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}

}
