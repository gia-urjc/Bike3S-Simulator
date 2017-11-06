package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class SimulationRandom {
	
	private static SimulationRandom instance = null;
	private Random random;
	
	private SimulationRandom(long seed) {
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
	
	public static SimulationRandom createRandom(long seed) {
		if(instance == null) {
			instance = new SimulationRandom(seed);
			return instance;
		}
		return instance;
	}
	
	public static SimulationRandom createRandom() {
		if(instance == null) {
			throw new IllegalStateException("You should call first createRandom(seed)");
		}
		return instance;
	}

}
