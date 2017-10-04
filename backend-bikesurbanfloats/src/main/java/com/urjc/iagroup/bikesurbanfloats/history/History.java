package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class History {

    static Gson gson = new Gson();

    private static HistoryEntry lastEntry;
    private static HistoryEntry nextEntry;

    private static ArrayList<JsonObject> serializedEntries = new ArrayList<>();

    static JsonObject idChange(Entity oldEntity, Entity newEntity) {
        JsonObject id = new JsonObject();

        if (oldEntity == null && newEntity == null) return null;

        if (oldEntity == null && newEntity != null) {
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        if (oldEntity != null && newEntity == null) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            return id;
        }

        if (oldEntity.getId() != newEntity.getId()) {
            id.add("old", new JsonPrimitive(oldEntity.getId()));
            id.add("new", new JsonPrimitive(newEntity.getId()));
            return id;
        }

        return null;
    }

    public static void init(List<EventUserAppears> userAppearsList) {
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(SystemInfo.stations.stream().collect(Collectors.toMap(Station::getId, HistoricStation::new)));

        List<JsonObject> users = new ArrayList<>();

        for (EventUserAppears event : userAppearsList) {
            JsonObject serializedUser = new JsonObject();
            Person user = event.getUser();

            serializedUser.add("appearsOn", new JsonPrimitive(event.getInstant()));
            // TODO: add other properties of user

            users.add(serializedUser);
        }

        // TODO: serialize stations and write json to file
        
        List<JsonObject> stations = new ArrayList<>();
        for(Station station: stations) {
        	
        }

    }

    public static void register(int timeInstant, Person user, Station station, Bike bike) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            serializedEntries.add(serializeChanges());
            lastEntry = nextEntry;
            nextEntry = new HistoryEntry(timeInstant, lastEntry);
        }

        // TODO: update entities in nextEntry
    }

    private static JsonObject serializeChanges() {
        JsonObject entry = new JsonObject();

        List<JsonObject> users = new ArrayList<>();

        for (HistoricUser user : nextEntry.getUsers().values()) {
            JsonObject changes = user.getChanges(lastEntry.getUsers().get(user.getId()));
            if (changes != null) users.add(changes);
        }

        if (!users.isEmpty()) {
            entry.add("users", gson.toJsonTree(users));
        }

        // TODO: serialize station changes (and bikes?)

        return entry;
    }

}
