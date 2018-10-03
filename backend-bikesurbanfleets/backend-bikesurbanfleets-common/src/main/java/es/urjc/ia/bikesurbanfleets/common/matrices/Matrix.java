package es.urjc.ia.bikesurbanfleets.common.matrices;

import java.util.Map.Entry;
import java.util.stream.Collectors;


public class Matrix {
    
    private Map<MatrixRoute, MatrixValue> matrix;

    public Matrix() {
    }

    public Matrix(Map<MatrixRoute, MatrixValue> matrix) {
        this.matrix = matrix;
    }
    public Map<MatrixRoute, MatrixValue> getMatrix() {
        return this.matrix;
    }

    @Override
    public String toString() {
        String result = matrix.entrySet().stream().map(Entry::toString).collect(Collectors.joining(";", "[", "]"));
            return "{" +
                " matrix='" + result + "'" +
            "}";
    }

}
