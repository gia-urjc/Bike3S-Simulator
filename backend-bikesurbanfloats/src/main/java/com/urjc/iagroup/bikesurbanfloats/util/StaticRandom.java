package com.urjc.iagroup.bikesurbanfloats.util;

import java.util.Random;

public class StaticRandom {
	
	private static Random random;
	
	public static void setSeed(long seed) {
		random = new Random(seed);
	}
	
	public static int nextInt(int min, int max) {	
		return min + random.nextInt((max - min) + 1);
	}
	
	public static int nextInt() {
		return random.nextInt();
	}
	
	public static double nextDouble(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}
	
	public static double nextDouble() {
		return random.nextDouble();
	}
	
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}


}
