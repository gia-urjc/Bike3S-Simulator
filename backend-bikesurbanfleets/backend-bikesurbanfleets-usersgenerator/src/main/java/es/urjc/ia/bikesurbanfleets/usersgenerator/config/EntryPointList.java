package es.urjc.ia.bikesurbanfleets.usersgenerator.config;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.deserializers.EntryPointDeserializer;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint;

import java.util.List;

public class EntryPointList {

    private int totalSimulationTime;

    private int randomSeed;

    @JsonAdapter(EntryPointDeserializer.class)
    private List<EntryPoint> entryPoints;

    public int getRandomSeed() {
        return randomSeed;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }


}
