package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIdentifier("stations")
public class HistoricStation implements HistoricEntity {

    private static Function<Bike, Integer> bikeIdConverter = bike -> bike == null ? null : bike.getId();

    @Expose
    private int id;

    @Expose
    private GeoPoint position;

    @Expose
    private int capacity;

    @Expose
    private List<Integer> bikes;

    private int reservedBikes;
    private int reservedSlots;
    private int bikesAvailable;
    private int slotsAvailable;

    public HistoricStation(Station station) {
        this.id = station.getId();
        this.position = new GeoPoint(station.getPosition());
        this.capacity = station.getCapacity();
        this.bikes = station.getBikes().stream().map(bikeIdConverter).collect(Collectors.toList());
        this.reservedBikes = station.getReservedBikes();
        this.reservedSlots = station.getReservedSlots();
        this.bikesAvailable = station.availableBikes();
        this.slotsAvailable = station.availableSlots();
    }

    @Override
    public int getId() {
        return id;
    }
}
