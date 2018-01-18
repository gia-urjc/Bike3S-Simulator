package es.urjc.ia.bikesurbanfleets.usersgenerator;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.deserializers.EntryPointDeserializer;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;

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
