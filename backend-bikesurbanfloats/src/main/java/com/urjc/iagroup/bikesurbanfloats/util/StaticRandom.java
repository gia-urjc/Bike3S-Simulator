package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class StaticRandom {
	
	private static StaticRandom instance = null;
	private Random random;
	
	private StaticRandom(long seed) {
		this.random = new Random(seed);
	}
	
	public void setSeed(long seed) {
		random = new Random(seed);
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
	
	public static StaticRandom createRandom(long seed) {
		if(instance == null) {
			instance = new StaticRandom(seed);
			return instance;
		}
		return instance;
	}
	
	public static StaticRandom createRandom() {
		if(instance == null) {
			throw new IllegalStateException("You should call first createRandom(seed)");
		}
		return instance;
	}

}
