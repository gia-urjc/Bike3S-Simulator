package es.urjc.ia.bikesurbanfleets.common.matrices;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MatrixValue {

    private Map<Integer, Double> instantProbabilityMap;


    public MatrixValue() {
    }

    public MatrixValue(Map<Integer, Double> instantProbabilityMap) {
        this.instantProbabilityMap = instantProbabilityMap;
    }

    public Map<Integer, Double> getInstantProbabilityList() {
        return this.instantProbabilityMap;
    }

    public String toString() {
        String result = instantProbabilityMap.entrySet().stream().map(Entry::toString).collect(Collectors.joining(";", "[", "]"));
        return "{" +
            " matrix='" + result + "'" +
        "}";
    }
    
}