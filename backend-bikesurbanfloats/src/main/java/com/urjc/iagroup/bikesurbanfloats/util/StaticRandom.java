package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class StaticRandom {
	
	private static StaticRandom generalInstance = null;
	private static StaticRandom userCreationInstance = null;

    public static void init(long seed) {
        if (generalInstance != null || userCreationInstance != null) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        generalInstance = new StaticRandom(seed);
        userCreationInstance = new StaticRandom(seed);
    }

    public static StaticRandom getGeneralInstance() {
        if (generalInstance != null) return generalInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

    public static StaticRandom getUserCreationInstance() {
        if (userCreationInstance != null) return userCreationInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

	private Random random;
	
	private StaticRandom(long seed) {
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
