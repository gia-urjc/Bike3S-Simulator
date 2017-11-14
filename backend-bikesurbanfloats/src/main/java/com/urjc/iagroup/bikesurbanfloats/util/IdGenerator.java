package com.urjc.iagroup.bikesurbanfloats.util;

/**
 * This class is used to automatically generate identifiers for the different entities at the system.
 * @author IAgroup
 *
 */
public class IdGenerator {

    private int counter = 0;

    public int next() {
        return counter++;
    }
}
