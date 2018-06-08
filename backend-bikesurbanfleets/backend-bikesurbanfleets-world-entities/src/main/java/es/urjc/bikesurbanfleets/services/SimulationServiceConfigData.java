package es.urjc.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.util.List;

public class SimulationServiceConfigData {

    private List<Station> stations;
    private BoundingBox bbox;

    private GraphManagerType graphManagerType;
    private RecommendationSystemType recomSystemType;
    private String mapDir;
    private Integer maxDistance;

    public SimulationServiceConfigData() {}

    public GraphManagerType getGraphManagerType() {
        return graphManagerType;
    }

    public SimulationServiceConfigData setGraphManagerType(GraphManagerType graphManagerType) {
        this.graphManagerType = graphManagerType;
        return this;
    }

    public RecommendationSystemType getRecomSystemType() {
        return recomSystemType;
    }

    public SimulationServiceConfigData setRecomSystemType(RecommendationSystemType recomSystemType) {
        this.recomSystemType = recomSystemType;
        return this;
    }

    public String getMapDir() {
        return mapDir;
    }

    public SimulationServiceConfigData setMapDir(String mapDir) {
        this.mapDir = mapDir;
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

    public SimulationServiceConfigData setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public int getMaxDistance() {
        return this.maxDistance;
    }

}
