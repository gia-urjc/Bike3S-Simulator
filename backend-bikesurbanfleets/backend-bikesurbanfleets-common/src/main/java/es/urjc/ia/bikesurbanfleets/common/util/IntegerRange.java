package es.urjc.ia.bikesurbanfleets.common.util;

/**
 * This class represent a range with a minimum inclusive and a maximum exclusive
 */
public class IntegerRange {

    private int minimum;

    private int maximum;

    /**
     * Receive the two values of a range and creates it
     * @param minimum
     * @param maximum
     * @throws IllegalArgumentException when minimum > maximum
     */
    public IntegerRange(int minimum, int maximum) {
        if (maximum < minimum) {
            throw new IllegalArgumentException("Minimum is greater than maximum");
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public boolean contains(int value) {
        return value >= minimum && value < maximum;
    }

    @Override
    public String toString() {
        return "IntegerRange{" +
                "minimum=" + minimum +
                ", maximum=" + maximum +
                '}';
    }
}
