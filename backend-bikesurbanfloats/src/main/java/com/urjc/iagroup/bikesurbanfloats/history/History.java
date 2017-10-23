package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
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
        nextEntry = new HistoryEntry(0);
        nextEntry.getStations().putAll(systemInfo.getStations().stream().collect(Collectors.toMap(Entity::getId, HistoricStation::new)));
        JsonObject entry = new JsonObject();
        
        List<JsonObject> users = new ArrayList<>();
        
        for (EventUserAppears event : userAppearsList) {
            JsonObject serializedUser = new JsonObject();
            Person person = event.getUser();
            nextEntry.getUsers().put(person.getId(), new HistoricUser(person));
            
            serializedUser.add("appearsOn", new JsonPrimitive(event.getInstant()));
            serializedUser.add("user", gson.toJsonTree(person, UserModel.class));

            users.add(serializedUser);
        }

        nextEntry.getBikes().putAll(systemInfo.getBikes().stream().collect(Collectors.toMap(Entity::getId, HistoricBike::new)));
           
        entry.add("users", gson.toJsonTree(users));
        entry.add("stations", gson.toJsonTree(systemInfo.getStations()));
        entry.add("bikes", gson.toJsonTree(systemInfo.getBikes()));
        
        serializedEntries.add(entry);
    }

    public static void register(int timeInstant, SystemInfo systemInfo) {
        if (timeInstant > nextEntry.getTimeInstant()) {
            serializedEntries.add(serializeChanges());

            // TODO: write serializedEntries to file and clear list

            lastEntry = nextEntry;
        }

        nextEntry = new HistoryEntry(timeInstant);
        nextEntry.getStations().putAll(systemInfo.getStations().stream().collect(Collectors.toMap(Entity::getId, HistoricStation::new)));
        nextEntry.getUsers().putAll(systemInfo.getPersons().stream().collect(Collectors.toMap(Entity::getId, HistoricUser::new)));
        nextEntry.getBikes().putAll(systemInfo.getBikes().stream().collect(Collectors.toMap(Entity::getId, HistoricBike::new)));
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

        return entry;
    }

}
