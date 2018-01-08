package es.urjc.ia.bikesurbanfleets.usersgenerator.common;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.usersgenerator.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.common.deserializers.EntryPointDeserializer;

import java.util.List;

public class EntryPointList {

    @JsonAdapter(EntryPointDeserializer.class)
    private List<EntryPoint> entryPoints;

}
