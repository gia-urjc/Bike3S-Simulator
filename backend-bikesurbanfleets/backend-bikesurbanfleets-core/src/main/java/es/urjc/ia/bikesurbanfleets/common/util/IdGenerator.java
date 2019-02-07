package es.urjc.ia.bikesurbanfleets.common.util;

/**
 * This class is used to automatically generate identifiers for the different entities at the system.
 * @author IAgroup
 *
 */
public class IdGenerator {

    private int counter = 1;

    public int next() {
        return counter++;
    }
}
