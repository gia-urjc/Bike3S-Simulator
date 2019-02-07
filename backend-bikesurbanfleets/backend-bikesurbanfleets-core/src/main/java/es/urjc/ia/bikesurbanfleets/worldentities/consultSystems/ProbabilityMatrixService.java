package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class ProbabilityMatrixService {

    private Map<Integer, Map<Integer, Map<Integer, Double>>> probabilityMatrix = new HashMap<>();

    public ProbabilityMatrixService(Map<String, Map<String, Map<String, Double>>> originalProbMatrix) {
        // In this constructor we reverse the latest matrix to have it indexed by Station origin id and instant
        originalProbMatrix.forEach((origStationId, mapStationDestination) -> {
            
            if(probabilityMatrix.get(Integer.parseInt(origStationId)) == null) 
                probabilityMatrix.put(Integer.parseInt(origStationId), new HashMap<>());

            mapStationDestination.forEach((destinationStationId, mapProbabilityByInstant) -> {
                mapProbabilityByInstant.forEach((instant, probability) -> {
                    
                    //The instant in matrices are in hours
                    if(probabilityMatrix.get(Integer.parseInt(origStationId)).get(Integer.parseInt(instant)) == null)
                        probabilityMatrix.get(Integer.parseInt(origStationId)).put(Integer.parseInt(instant), new HashMap<>());
                    
                    probabilityMatrix.get(Integer.parseInt(origStationId)).get(Integer.parseInt(instant)).put(Integer.parseInt(destinationStationId), probability);
                });
            });
        });
    }

    public Map<Integer, Double> getProbByStationOrigAndInstant(int stationOriginId, int instant) {
        return probabilityMatrix.get(stationOriginId).get(instant);
    }

    public static void main(String[] args) throws FileNotFoundException {
    
        String matrices = "backend-configuration-files/matrices.json";
    
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(matrices));
        JsonElement element = gson.fromJson(reader, JsonElement.class);
        System.out.println(element.getAsJsonObject().get("probabilityMatrixWeek"));

        //Type type = new TypeToken<Map<Integer, Map<Integer, Map<Integer, Double>>>>(){}.getType();
        //gson.fromJson()
    
    }

}