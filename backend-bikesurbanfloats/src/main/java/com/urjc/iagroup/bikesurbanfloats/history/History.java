package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class History {

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    static Gson gson = new Gson();

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    public static void init() {
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(SystemInfo.stations.stream().collect(Collectors.toMap(Station::getId, HistoricStation::new)));
    }

    public static void register(int timeInstant, Person user, Station station, Bike bike) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            serializedEntries.add(serializeNext());
            lastEntry = nextEntry;
            nextEntry = new HistoryEntry(timeInstant, lastEntry);
        }

        // TODO: update entities in nextEntry
    }

    private static JsonObject serializeNext() {
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();

        for (HistoricUser user : nextEntry.getUsers().values()) {
            JsonObject changes = user.getChanges(lastEntry.getUsers().get(user.getId()));
            if (changes != null) users.add(changes);
        }

        if (!users.isEmpty()) {
            entry.add("users", gson.toJsonTree(users));
        }

        // TODO: serialize stations (and bikes?)

        return entry;
    }

}
