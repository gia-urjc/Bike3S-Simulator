package com.urjc.iagroup.bikesurbanfloats.history.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.models.StationModel;
import com.urjc.iagroup.bikesurbanfloats.history.HistoricEntity;
import com.urjc.iagroup.bikesurbanfloats.history.JsonIdentifier;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIdentifier("stations")
public class HistoricStation implements HistoricEntity<HistoricStation>, StationModel<HistoricBike> {

    private static Function<Bike, HistoricBike> bikeConverter = bike -> bike == null ? null : new HistoricBike(bike);

    private int id;

    private GeoPoint position;

    private int capacity;

    private List<HistoricBike> bikes;

    private int reservedBikes;
    private int reservedSlots;
    private int bikesAvailable;
    private int slotsAvailable;

    public HistoricStation(Station station) {
        this.id = station.getId();
        this.position = new GeoPoint(station.getPosition());
        this.capacity = station.getCapacity();
        this.bikes = station.getBikes().stream().map(bikeConverter).collect(Collectors.toList());
        this.reservedBikes = station.getReservedBikes();
        this.reservedSlots = station.getReservedSlots();
        this.bikesAvailable = station.availableBikes();
        this.slotsAvailable = station.availableSlots();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public GeoPoint getPosition() {
        return position;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public List<HistoricBike> getBikes() {
        return bikes;
    }

    @Override
    public int getReservedBikes() {
        return reservedBikes;
    }

    @Override
    public int getReservedSlots() {
        return reservedSlots;
    }

    @Override
    public int availableBikes() {
        return bikesAvailable;
    }

    @Override
    public int availableSlots() {
        return slotsAvailable;
    }

    @Override
	public JsonObject makeChangeEntryFrom(HistoricStation previousSelf) {
        JsonObject changes = HistoricEntity.super.makeChangeEntryFrom(previousSelf);

        if (changes == null) return null;

        boolean hasChanges = false;

        changes.add("id", new JsonPrimitive(id));

        JsonObject bikes = new JsonObject();

        for (int i = 0; i < capacity; i++) {
            JsonObject bike = HistoricEntity.idReferenceChange(previousSelf.bikes.get(i), this.bikes.get(i));
            if (bike != null) {
                bikes.add(Integer.toString(i), bike);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            changes.add("bikes", bikes);
        }

        // TODO: add other possible changes

        return hasChanges ? changes : null;
	}

}
