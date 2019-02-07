/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.common.util;

import java.util.Random;

/**
 *
 * @author holger
 */
public class SimpleRandom {
    private Random random;

    public SimpleRandom(long seed) {
        this.random = new Random(seed);
    }

    public int nextInt(int min, int max) {
        return min + random.nextInt((max - min));
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
