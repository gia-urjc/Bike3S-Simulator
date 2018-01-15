package es.urjc.ia.bikesurbanfleets.usersgenerator.config.deserializers;

import com.google.gson.*;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPoint.EntryPointType;
import es.urjc.ia.bikesurbanfleets.usersgenerator.config.entrypoints.EntryPointFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to obtain the entry points in the system's own format.
 * @author IAgroup
 *
 */
public class EntryPointDeserializer implements JsonDeserializer<List<EntryPoint>> {

    private EntryPointFactory entryPointFactory;

    public EntryPointDeserializer() {
        this.entryPointFactory = new EntryPointFactory();
    }

    @Override
    public List<EntryPoint> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<EntryPoint> entryPoints = new ArrayList<>();

        for (JsonElement element : json.getAsJsonArray()) {
            JsonObject jsonEntryPoint = element.getAsJsonObject();
            EntryPointType entryPointType;
            entryPointType = EntryPointType.valueOf(jsonEntryPoint.get("entryPointType").getAsString());
            entryPoints.add(entryPointFactory.createEntryPoint(jsonEntryPoint, entryPointType));
        }

        return entryPoints;
    }

}
