package com.urjc.iagroup.bikesurbanfloats.history;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.stream.Collectors;

public class History {

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    public static void init() {
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(SystemInfo.stations.stream().collect(Collectors.toMap(Station::getId, HistoricStation::new)));
    }

    public static void register(int timeInstant, Person user, Station station, Bike bike) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            lastEntry = nextEntry;
            nextEntry = new HistoryEntry(timeInstant, lastEntry);
            // TODO: write lastEntry to json
        } else {
            // TODO: update entities in nextEntry
        }
    }

}
