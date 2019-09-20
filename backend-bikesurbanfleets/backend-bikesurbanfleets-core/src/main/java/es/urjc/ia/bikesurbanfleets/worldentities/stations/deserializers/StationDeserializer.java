package es.urjc.ia.bikesurbanfleets.worldentities.stations.deserializers;

import com.google.gson.*;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Bike;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to obtain the stations in the system's own format.
 *
 * @author IAgroup
 *
 */
public class StationDeserializer implements JsonDeserializer<Station> {

    private static final String JSON_ATTR_BIKES = "bikes";
    private static final String JSON_ATTR_CAPACITY = "capacity";
    private static final String JSON_ATTR_POSITION = "position";
    private static final String JSON_ATTR_OFICIALID = "id";

    @Override
    public Station deserialize(JsonElement jsonstation, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject station = jsonstation.getAsJsonObject();
        JsonElement jsonElementBikes = station.get(JSON_ATTR_BIKES);
        int capacity = station.get(JSON_ATTR_CAPACITY).getAsInt();
        int oficialID = station.get(JSON_ATTR_OFICIALID).getAsInt();
        List<Bike> bikes = new ArrayList<>();

        boolean isArray = jsonElementBikes.isJsonArray();
        JsonArray jsonArrayBikes = isArray ? jsonElementBikes.getAsJsonArray() : null;
        int n = isArray ? jsonArrayBikes.size() : jsonElementBikes.getAsInt();
        int naux = capacity - n;
        for (int i = 0; i < n; i++) {
            // TODO: check if the deserialization context actually calls the bike constructor
            Bike bike = isArray ? context.deserialize(jsonArrayBikes.get(i), Bike.class) : new Bike();
            bikes.add(bike);
        }
        for (int i = 0; i < naux; i++) {
            bikes.add(null);
        }

        JsonElement jsonElemGeoP = jsonstation.getAsJsonObject().get(JSON_ATTR_POSITION);
        GeoPoint position = context.deserialize(jsonElemGeoP, GeoPoint.class);

        return new Station(position, capacity, bikes, oficialID);
    }

}
