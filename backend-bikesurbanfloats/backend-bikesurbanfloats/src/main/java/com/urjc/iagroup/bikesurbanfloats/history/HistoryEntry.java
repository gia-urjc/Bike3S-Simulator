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
