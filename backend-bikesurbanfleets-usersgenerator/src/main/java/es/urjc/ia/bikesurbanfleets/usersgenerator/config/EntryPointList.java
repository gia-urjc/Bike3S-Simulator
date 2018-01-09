package es.urjc.ia.bikesurbanfleets.usersgenerator.config;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.deserializers.EntryPointDeserializer;

import java.util.List;

public class EntryPointList {

    @JsonAdapter(EntryPointDeserializer.class)
    private List<EntryPoint> entryPoints;

}
