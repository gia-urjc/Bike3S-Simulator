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
public class SimulationRandom extends SimpleRandom{

    private static SimulationRandom Instance = null;
    private static List<GeoPoint> validRandomPositions = new ArrayList<>();

/*    public static void init() {
        if (Instance != null ) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        Instance = new SimulationRandom();
    }
*/
    public static void init(long seed) {
        if (Instance != null ) {
            throw new IllegalStateException("Instances have already been initialized.");
        }

        Instance = new SimulationRandom(seed);
    }

    public static SimulationRandom getInstance() {
        if (Instance != null) return Instance;
        throw new IllegalStateException("You should first call init(seed)");
    }

 /*   public static boolean addRandomUsedPoint(GeoPoint point) {
        return validRandomPositions.add(point);
    }

    public static GeoPoint getRandomUsedPoint() throws IllegalAccessException {
        int index = generalInstance.nextInt(0, validRandomPositions.size() - 1);
        if(validRandomPositions.size() == 0) {
            throw new IllegalAccessException("No valid random points yet");
        }
        return validRandomPositions.get(index);
    }
*/
    private Random random;

    private SimulationRandom(long seed) {
        super(seed);
    }
 }
