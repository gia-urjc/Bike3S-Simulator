package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class SimulationRandom {
	
	private static SimulationRandom generalInstance = null;
	private static SimulationRandom userCreationInstance = null;

    public static void init(long seed) {
        if (generalInstance != null || userCreationInstance != null) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        generalInstance = new SimulationRandom(seed);
        userCreationInstance = new SimulationRandom(seed);
    }

    public static SimulationRandom getGeneralInstance() {
        if (generalInstance != null) return generalInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

    public static SimulationRandom getUserCreationInstance() {
        if (userCreationInstance != null) return userCreationInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

	private Random random;
	
	private SimulationRandom(long seed) {
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
