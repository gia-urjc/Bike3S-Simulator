package es.urjc.ia.bikesurbanfleets.usersgenerator.config;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.deserializers.EntryPointDeserializer;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoint.EntryPoint;

import java.util.List;

public class EntryPointInfo {


    @JsonAdapter(EntryPointDeserializer.class)
    private List<EntryPoint> entryPoints;

    public List<EntryPoint> getEntryPoints() {
        return entryPoints;
    }


}
