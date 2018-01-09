package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Reservation;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.History.IdReference;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * It contains the rlevant information of a specific station, e. g., its history.
 * @author IAgroup
 *
 */
@JsonIdentifier("stations")
public class HistoricStation implements HistoricEntity {

    @Expose
    private int id;

    @Expose
    private GeoPoint position;

    @Expose
    private int capacity;

    @Expose
    private IdReference bikes;

    private IdReference reservations;

    public HistoricStation(Station station) {
        this.id = station.getId();
        this.position = new GeoPoint(station.getPosition());
        this.capacity = station.getCapacity();

        this.bikes = new IdReference(
                HistoricBike.class,
                station.getBikes().stream()
                        .map(bike -> bike == null ? null : bike.getId())
                        .collect(Collectors.toList())
        );

        this.reservations = new IdReference(
                HistoricReservation.class,
                station.getReservations().stream()
                        .map(Reservation::getId)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public int getId() {
        return id;
    }
}
