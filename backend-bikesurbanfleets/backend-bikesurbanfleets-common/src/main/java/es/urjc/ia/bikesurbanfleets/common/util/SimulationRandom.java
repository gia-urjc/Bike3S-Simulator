package es.urjc.ia.bikesurbanfleets.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;

/**
 * This class is a random type initialized with a specific seed.
 * It contains two unique instances: one to use it in the users creation and another to
 * the rest of cases.
 * It provides methods to randomly generate data primitive types.
 * @author IAgroup
 *
 */
public class SimulationRandom {

    private static SimulationRandom generalInstance = null;
    private static SimulationRandom userCreationInstance = null;
    private static List<GeoPoint> validRandomPositions = new ArrayList<>();

    public static void init() {
        if (generalInstance != null || userCreationInstance != null) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        generalInstance = new SimulationRandom();
        userCreationInstance = new SimulationRandom();
    }

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

    public static boolean addRandomUsedPoint(GeoPoint point) {
        return validRandomPositions.add(point);
    }

    public static GeoPoint getRandomUsedPoint() throws IllegalAccessException {
        int index = generalInstance.nextInt(0, validRandomPositions.size() - 1);
        if(validRandomPositions.size() == 0) {
            throw new IllegalAccessException("No valid random points yet");
        }
        return validRandomPositions.get(index);
    }

    private Random random;

    private SimulationRandom() {
        this.random = new Random();
    }

    private SimulationRandom(long seed) {
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
