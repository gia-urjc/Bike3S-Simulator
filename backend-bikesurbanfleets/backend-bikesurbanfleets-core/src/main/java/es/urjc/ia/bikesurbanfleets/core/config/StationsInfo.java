package es.urjc.ia.bikesurbanfleets.core.config;

import com.google.gson.annotations.JsonAdapter;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.deserializers.StationDeserializer;

import java.util.List;

public class StationsInfo {

    /**
     * They are all the stations of the system obtained from the configuration file.
     */
    @JsonAdapter(StationDeserializer.class)
    private List<Station> stations;

    public List<Station> getStations() { return stations; }

}
