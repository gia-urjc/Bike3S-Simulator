package com.urjc.iagroup.bikesurbanfloats.util;

public class IdGenerator {

    private int counter = 0;

    public int next() {
        return counter++;
    }
}
