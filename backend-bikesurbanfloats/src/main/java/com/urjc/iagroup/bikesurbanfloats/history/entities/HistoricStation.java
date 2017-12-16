package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.History.IdReference;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

import java.util.List;
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
    private static Function<Bike, IdReference> bikeIdConverter = bike -> new IdReference(HistoricBike.class, bike == null ? null : bike.getId());

    @Expose
    private int id;

    @Expose
    private GeoPoint position;

    @Expose
    private int capacity;

    @Expose
    private List<IdReference> bikes;

    public HistoricStation(Station station) {
        this.id = station.getId();
        this.position = new GeoPoint(station.getPosition());
        this.capacity = station.getCapacity();
        this.bikes = station.getBikes().stream().map(bikeIdConverter).collect(Collectors.toList());
    }

    @Override
    public int getId() {
        return id;
    }
}
