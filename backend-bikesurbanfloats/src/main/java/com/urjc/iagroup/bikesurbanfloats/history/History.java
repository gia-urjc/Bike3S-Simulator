package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class History {

    static Gson gson = new Gson();

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    public static void init(List<EventUserAppears> userAppearsList, SystemInfo systemInfo) {
        lastEntry = new HistoryEntry(0);
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(systemInfo.getStations().stream().collect(Collectors.toMap(Entity::getId, HistoricStation::new)));
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();

        for (EventUserAppears event : userAppearsList) {
            JsonObject serializedUser = new JsonObject();
            User user = event.getUser();
            nextEntry.getUsers().put(user.getId(), new HistoricUser(user));

            serializedUser.add("timeInstant", new JsonPrimitive(event.getInstant()));
            serializedUser.add("user", gson.toJsonTree(user, UserModel.class));

            users.add(serializedUser);
        }


        nextEntry.getBikes().putAll(systemInfo.getBikes().stream().collect(Collectors.toMap(Entity::getId, HistoricBike::new)));

        entry.add("userAppearanceEvents", gson.toJsonTree(users));
        entry.add("stations", gson.toJsonTree(systemInfo.getStations()));
        // entry.add("bikes", gson.toJsonTree(SystemInfo.bikes));

        // TODO: write file
    }

    public static void register(int timeInstant, User user, Station station, Bike bike) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            JsonObject changes = serializeChanges();

            if (changes != null) {
                serializedEntries.add(changes);
            }

            // TODO: write serializedEntries to file and clear list

            lastEntry = nextEntry;

            nextEntry = new HistoryEntry(timeInstant);
        }

        if (user != null) nextEntry.getUsers().put(user.getId(), new HistoricUser(user));
        if (station != null) nextEntry.getStations().put(station.getId(), new HistoricStation(station));
        if (bike != null) nextEntry.getBikes().put(bike.getId(), new HistoricBike(bike));
    }

    private static JsonObject serializeChanges() {
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();
        List<JsonObject> stations = new ArrayList<>();

        for (HistoricUser user : nextEntry.getUsers().values()) {
            JsonObject changes = user.makeChangeEntryFrom(lastEntry.getUsers().get(user.getId()));
            if (changes != null) users.add(changes);
        }

        if (!users.isEmpty()) {
            entry.add("users", gson.toJsonTree(users));
        }

        for (HistoricStation station : nextEntry.getStations().values()) {
            JsonObject changes = station.makeChangeEntryFrom(lastEntry.getStations().get(station.getId()));
            if (changes != null) stations.add(changes);
        }

        if(!stations.isEmpty()) {
            entry.add("stations", gson.toJsonTree(stations));
        }

        return entry.entrySet().isEmpty() ? null : entry;
    }

}
