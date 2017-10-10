package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

public class HistoryEntry {

    private int timeInstant;
    private Map<Integer, HistoricPerson> persons;
    private Map<Integer, HistoricStation> stations;
    private Map<Integer, HistoricBike> bikes;

    HistoryEntry(int timeInstant) {
        this.timeInstant = timeInstant;
        this.persons = new HashMap<>();
        this.stations = new HashMap<>();
        this.bikes = new HashMap<>();
    }

    HistoryEntry(int timeInstant, HistoryEntry entry) {
        this(timeInstant);
        this.persons.putAll(entry.persons);
        this.stations.putAll(entry.stations);
        this.bikes.putAll(entry.bikes);
    }

    int getTimeInstant() {
        return timeInstant;
    }

    Map<Integer, HistoricPerson> getPersons() {
        return persons;
    }

    Map<Integer, HistoricStation> getStations() {
        return stations;
    }

    Map<Integer, HistoricBike> getBikes() {
        return bikes;
    }
}
