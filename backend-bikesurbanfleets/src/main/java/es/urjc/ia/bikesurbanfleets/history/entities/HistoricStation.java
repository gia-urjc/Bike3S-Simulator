package es.urjc.ia.bikesurbanfleets.history.entities;

import com.google.gson.annotations.Expose;
import es.urjc.ia.bikesurbanfleets.entities.Bike;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.history.HistoricEntity;
import es.urjc.ia.bikesurbanfleets.history.History.IdReference;
import es.urjc.ia.bikesurbanfleets.history.JsonIdentifier;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * It contains the rlevant information of a specific station, e. g., its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("stations")
public class HistoricStation implements HistoricEntity {
    /**
     * This lambda function returns the bike id if the bike instance isn't null 
     * or null in other case.
     */
    private static Function<Bike, Integer> bikeIdConverter = bike -> bike == null ? null : bike.getId();

    @Expose
    private int id;

    @Expose
    private GeoPoint position;

    @Expose
    private int capacity;

    @Expose
    private IdReference bikes;

    public HistoricStation(Station station) {
        this.id = station.getId();
        this.position = new GeoPoint(station.getPosition());
        this.capacity = station.getCapacity();
        this.bikes = new IdReference(HistoricBike.class, station.getBikes().stream().map(bikeIdConverter).collect(Collectors.toList()));
    }

    @Override
    public int getId() {
        return id;
    }
}
