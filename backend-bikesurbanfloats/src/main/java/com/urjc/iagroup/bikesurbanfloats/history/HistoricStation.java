package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.stream.Collectors;

class HistoricStation extends Station {

    HistoricStation(Station station) {
        super(
                station.getId(),
                station.getPosition(),
                station.getCapacity(),
                station.getBikes().stream()
                        .map(bike -> bike == null ? null : new HistoricBike(bike))
                        .collect(Collectors.toList())
        );
    }

    JsonObject getChanges(HistoricStation previousSelf) {
        if (previousSelf == null) return null;

        JsonObject changes = new JsonObject();
        boolean hasChanges = false;

        changes.add("id", new JsonPrimitive(this.getId()));

        JsonObject bikes = new JsonObject();

        for (int i = 0; i < this.getCapacity(); i++) {
            JsonObject bike = History.idChange(previousSelf.getBikes().get(i), this.getBikes().get(i));
            if (bike != null) {
                bikes.add(Integer.toString(i), bike);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            changes.add("bikes", bikes);
        }

        return hasChanges ? changes : null;
    }
}