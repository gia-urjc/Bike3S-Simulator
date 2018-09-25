package es.urjc.ia.bikesurbanfleets.common.matrices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
