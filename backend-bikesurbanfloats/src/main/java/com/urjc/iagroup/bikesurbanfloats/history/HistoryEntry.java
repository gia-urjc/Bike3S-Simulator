package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

class HistoryEntry {

    private int timeInstant;
    private Map<Integer, Person> users;
    private Map<Integer, Station> stations;
    private Map<Integer, Bike> bikes;

    HistoryEntry(int timeInstant) {
        this.timeInstant = timeInstant;
        this.users = new HashMap<>();
        this.stations = new HashMap<>();
        this.bikes = new HashMap<>();
    }

    HistoryEntry(int timeInstant, HistoryEntry entry) {
        this(timeInstant);
        this.users.putAll(entry.users);
        this.stations.putAll(entry.stations);
        this.bikes.putAll(entry.bikes);
    }

    int getTimeInstant() {
        return timeInstant;
    }

    Map<Integer, Person> getUsers() {
        return users;
    }

    Map<Integer, Station> getStations() {
        return stations;
    }

    Map<Integer, Bike> getBikes() {
        return bikes;
    }
}
