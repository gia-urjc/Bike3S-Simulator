package es.urjc.ia.bikesurbanfleets.history;

import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;

/**
 * This class represents all the final global values that simulation has produced. It is used to store a history file
 * that contains such information
 */
public class FinalGlobalValues {

    /**
     * Time instant of the last Event executed
     */
    private int totalTimeSimulation;

    /**
     * Bounding Box where the simulation has been reproduced
     */
    private BoundingBox boundingBox;

    public FinalGlobalValues() {}

    public FinalGlobalValues(int totalTimeSimulation, BoundingBox BoundingBox) {
        this.totalTimeSimulation = totalTimeSimulation;
        this.boundingBox = BoundingBox;
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(BoundingBox BoundingBox) {
        this.boundingBox = BoundingBox;
    }

    public int getTotalTimeSimulation() {
        return this.totalTimeSimulation;
    }

    public void setTotalTimeSimulation(int totalTimeSimulation) {
        this.totalTimeSimulation = totalTimeSimulation;
    }

}