package com.urjc.iagroup.bikesurbanfloats.history;

import java.util.HashMap;
import java.util.Map;

class HistoryEntry {

    private int timeInstant;
    private Map<Integer, HistoricUser> users;
    private Map<Integer, HistoricStation> stations;
    private Map<Integer, HistoricBike> bikes;

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

    Map<Integer, HistoricUser> getUsers() {
        return users;
    }

    Map<Integer, HistoricStation> getStations() {
        return stations;
    }

    Map<Integer, HistoricBike> getBikes() {
        return bikes;
    }
}
