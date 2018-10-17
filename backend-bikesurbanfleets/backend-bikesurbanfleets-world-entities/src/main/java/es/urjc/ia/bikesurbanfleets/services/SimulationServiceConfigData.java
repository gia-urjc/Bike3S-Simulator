package es.urjc.ia.bikesurbanfleets.services;

import com.google.gson.JsonElement;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.util.List;

public class  SimulationServiceConfigData {

    private List<Station> stations;
    private BoundingBox bbox;

    private String graphManagerType;
    private String recomSystemType;
    private JsonElement graphParameters;
    private JsonElement recomParameters;

    public SimulationServiceConfigData() {}

    public String getGraphManagerType() {
        return graphManagerType;
    }

    public SimulationServiceConfigData setGraphManagerType(String graphManagerType) {
        this.graphManagerType = graphManagerType;
        return this;
    }

    public String getRecomSystemType() {
        return recomSystemType;
    }

    public SimulationServiceConfigData setRecomSystemType(String recomSystemType) {
        this.recomSystemType = recomSystemType;
        return this;
    }

    public JsonElement getGraphParameters() {
        return graphParameters;
    }

    public SimulationServiceConfigData setGraphParameters(JsonElement graphParameters) {
        this.graphParameters = graphParameters;
        return this;
    }

    public List<Station> getStations() {
        return stations;
    }

    public SimulationServiceConfigData setStations(List<Station> stations) {
        this.stations = stations;
        return this;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public SimulationServiceConfigData setBbox(BoundingBox bbox) {
        this.bbox = bbox;
        return this;
    }

    public SimulationServiceConfigData setRecomParameters(JsonElement recomparameters) {
        this.recomParameters = recomparameters;
        return this;
    }

    public JsonElement getRecomParameters() {
        return this.recomParameters;
    }

}
