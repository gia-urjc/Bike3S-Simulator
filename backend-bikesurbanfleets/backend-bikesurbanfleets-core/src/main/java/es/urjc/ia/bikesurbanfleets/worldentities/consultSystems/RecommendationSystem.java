package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems;

import java.util.List;
import java.util.stream.Collectors;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.recommendationSystemTypes.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

public abstract class RecommendationSystem {

    /**
     * It provides information about the infraestructure state.
     */
    protected InfraestructureManager infraestructureManager;

    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    protected static List<Station> validStationsToRentBike(List<Station> stations) {
        return stations.stream().filter(station -> station.availableBikes() > 0)
                .collect(Collectors.toList());
    }

    /**
     * It filters stations which have not available bikes.
     *
     * @return a list of stations with available bikes.
     */
    protected static List<Station> validStationsToReturnBike(List<Station> stations) {
        return stations.stream().filter((station) -> station.availableSlots() > 0)
                .collect(Collectors.toList());
    }

    public RecommendationSystem(InfraestructureManager infraestructureManager) {
        this.infraestructureManager = infraestructureManager;
    }

    public abstract List<Recommendation> recommendStationToRentBike(GeoPoint point);

    public abstract List<Recommendation> recommendStationToReturnBike(GeoPoint point);

    //auxiliary function to normalize values in a linear way to the range [0,1]
    protected double normatizeTo01(double value, double minvalue, double maxvalue){
        if (maxvalue<=minvalue) throw new RuntimeException("invalid program state");
        if (value<minvalue) throw new RuntimeException("invalid program state");
        if (value>maxvalue) throw new RuntimeException("invalid program state");
        return (value-minvalue)/(maxvalue-minvalue);
    }
    
}
