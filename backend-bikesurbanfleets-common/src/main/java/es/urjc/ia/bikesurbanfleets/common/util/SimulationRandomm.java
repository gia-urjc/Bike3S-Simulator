package es.urjc.ia.bikesurbanfleets.common.util;

import java.util.Random;

/**
 * This class is a random type initialized with a specific seed.
 * It contains two unique instances: one to use it in the user creation and another to
 * the rest of cases.
 * It provides methods to randomly generate data primitive types.
 * @author IAgroup
 *
 */
public class SimulationRandomm {

    private static SimulationRandomm generalInstance = null;
    private static SimulationRandomm userCreationInstance = null;

    public static void init(long seed) {
        if (generalInstance != null || userCreationInstance != null) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        generalInstance = new SimulationRandomm(seed);
        userCreationInstance = new SimulationRandomm(seed);
    }

    public static SimulationRandomm getGeneralInstance() {
        if (generalInstance != null) return generalInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

    public static SimulationRandomm getUserCreationInstance() {
        if (userCreationInstance != null) return userCreationInstance;
        throw new IllegalStateException("You should first call init(seed)");
    }

    private Random random;

    private SimulationRandomm(long seed) {
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
